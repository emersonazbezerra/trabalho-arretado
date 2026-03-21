# CLAUDE.md — web/ (Frontend Next.js)

Este é o frontend do **Trabalho Arretado**. Leia também `../docs/DOMAIN.md` e
`../docs/DATA_GUIDE.md` antes de qualquer tarefa de modelagem ou desenvolvimento.

---

## Stack

| Tecnologia | Versão | Papel |
|---|---|---|
| Next.js | 15.x (App Router) | Framework principal |
| React | 19.x | UI |
| TypeScript | 5.x | Tipagem estática |
| Tailwind CSS | 4.x | Estilização utilitária |
| shadcn/ui | latest | Componentes base |
| React Hook Form | 7.x | Formulários |
| Zod | 3.x | Validação de esquemas |
| nuqs | 2.x | Estado em query string (filtros de busca) |

---

## Estrutura de pastas

```
web/src/
├── app/                          ← App Router do Next.js
│   ├── layout.tsx                ← layout raiz
│   ├── page.tsx                  ← página inicial (/)
│   ├── profissional/
│   │   └── [slug]/
│   │       └── page.tsx          ← perfil público (/profissional/joao-silva-jp)
│   ├── buscar/
│   │   └── page.tsx              ← listagem com filtros (/buscar?category=&city=)
│   ├── cadastrar/
│   │   └── page.tsx              ← cadastro de profissional
│   └── api/                      ← route handlers Next.js (BFF se necessário)
├── components/
│   ├── ui/                       ← componentes shadcn/ui (gerados, não editar manualmente)
│   ├── layout/
│   │   ├── Header.tsx
│   │   └── Footer.tsx
│   ├── professional/
│   │   ├── ProfessionalCard.tsx  ← card na listagem
│   │   ├── ProfessionalProfile.tsx
│   │   ├── PortfolioGallery.tsx
│   │   └── ReviewList.tsx
│   └── search/
│       ├── SearchBar.tsx
│       └── CategoryFilter.tsx
├── lib/
│   ├── api.ts                    ← funções de fetch para a API backend
│   ├── utils.ts                  ← utilitários gerais (cn, formatPhone, etc.)
│   └── whatsapp.ts               ← geração de links wa.me com UTM
├── types/
│   └── api.ts                    ← interfaces TypeScript espelhando os DTOs Kotlin
└── hooks/
    ├── useProfessionals.ts
    └── useSearch.ts
```

---

## Convenções de código

### Componentes

- **Sempre** usar `function` nomeada (não arrow function) para Server Components
- Usar arrow function para Client Components (`"use client"`) por convenção visual
- Props em interface nomeada `NomeDoComponenteProps`
- Exportar como `default` apenas componentes de página (`page.tsx`, `layout.tsx`)
- Componentes reutilizáveis: **named export**

```tsx
// ✅ Server Component
export function ProfessionalCard({ professional }: ProfessionalCardProps) {
  return <div>...</div>
}

// ✅ Client Component
"use client"
const SearchBar = ({ onSearch }: SearchBarProps) => {
  return <form>...</form>
}
export { SearchBar }
```

### Fetch de dados

- Dados de página: buscar em Server Components com `fetch` nativo (Next.js cache)
- Dados interativos (filtros, paginação): usar `useSearchParams` + fetch no cliente
- **Nunca** usar `useEffect` para buscar dados que poderiam ser buscados no servidor
- Timeout padrão: 5 segundos para chamadas ao backend

```typescript
// lib/api.ts
const API_URL = process.env.NEXT_PUBLIC_API_URL

export async function getProfessionalBySlug(slug: string) {
  const res = await fetch(`${API_URL}/api/professionals/${slug}`, {
    next: { revalidate: 60 }   // ISR: revalida a cada 60 segundos
  })
  if (!res.ok) return null
  const json = await res.json()
  return json.data as ProfessionalProfile
}
```

### Estilização

- Usar **apenas classes Tailwind** — sem CSS modules, sem styled-components, sem `style={}`
- Utilitário `cn()` (de `lib/utils.ts`) para combinar classes condicionalmente:
  ```tsx
  import { cn } from "@/lib/utils"
  <div className={cn("base-class", isActive && "active-class")} />
  ```
- Breakpoints mobile-first: `sm:`, `md:`, `lg:` — o layout base é mobile
- Paleta de cores: definida em `tailwind.config.ts` como CSS variables

### Formulários

Sempre usar React Hook Form + Zod para validação:

```tsx
const schema = z.object({
  name: z.string().min(2, "Nome muito curto"),
  phone: z.string().regex(/^\d{10,11}$/, "Telefone inválido"),
})

const form = useForm<z.infer<typeof schema>>({
  resolver: zodResolver(schema),
})
```

---

## SEO — regras obrigatórias

Páginas de perfil de profissionais são o principal ativo de SEO. Obrigatório:

```tsx
// app/profissional/[slug]/page.tsx
export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const professional = await getProfessionalBySlug(params.slug)
  if (!professional) return {}
  return {
    title: `${professional.name} — ${professional.mainCategory} em ${professional.city} | Trabalho Arretado`,
    description: professional.bio?.slice(0, 160) ?? `Contrate ${professional.name}, profissional de ${professional.mainCategory} em ${professional.city}.`,
    openGraph: {
      images: [professional.avatarUrl ?? "/og-default.png"],
    },
  }
}
```

Structured data (JSON-LD) obrigatório em perfis:
- Schema `LocalBusiness` ou `Person` com `name`, `telephone`, `address`, `aggregateRating`

---

## Link WhatsApp — padrão obrigatório

Todo botão de contato deve usar a função centralizada de `lib/whatsapp.ts` para garantir
rastreamento de leads antes do redirecionamento:

```typescript
// lib/whatsapp.ts
export function buildWhatsAppUrl(phone: string, professionalSlug: string, source: string) {
  const message = encodeURIComponent(
    `Olá! Vi seu perfil no Trabalho Arretado e gostaria de um orçamento.`
  )
  return `https://wa.me/${phone}?text=${message}`
}
```

O clique no botão deve **primeiro** registrar o lead via `POST /api/leads` antes de redirecionar.

---

## Comandos npm

```bash
# Desenvolvimento
npm run dev

# Build de produção
npm run build

# Iniciar servidor de produção
npm start

# Verificar tipos
npm run type-check

# Linting
npm run lint

# Adicionar componente shadcn/ui
npx shadcn@latest add button
npx shadcn@latest add card
```

---

## Variáveis de ambiente necessárias

Ver `../docs/DATA_GUIDE.md` — seção "Variáveis de ambiente / web/.env.example".

Arquivo local: `web/.env.local` (nunca commitar — está no .gitignore).
