# DATA_GUIDE.md — Guia de Dados e Convenções

Convenções de nomenclatura, modelagem e serialização usadas em todo o projeto.
Seguir este guia garante consistência entre backend, frontend e banco de dados.

---

## Convenções de nomenclatura

| Camada | Convenção | Exemplo |
|---|---|---|
| Tabelas PostgreSQL | `snake_case`, plural | `professionals`, `portfolio_items` |
| Colunas PostgreSQL | `snake_case` | `created_at`, `avatar_url` |
| Classes Kotlin | `PascalCase` | `Professional`, `ServiceOffer` |
| Campos Kotlin | `camelCase` | `avatarUrl`, `createdAt` |
| Rotas da API | `kebab-case`, plural | `/api/service-categories` |
| Slugs de URL | `kebab-case` | `/profissional/joao-silva-eletricista-jp` |
| Variáveis de ambiente | `SCREAMING_SNAKE_CASE` | `DATABASE_URL`, `R2_BUCKET_NAME` |
| Componentes React | `PascalCase` | `ProfessionalCard`, `SearchBar` |
| Hooks React | `camelCase` com prefixo `use` | `useProfessionals`, `useSearch` |
| Arquivos de componente | `PascalCase.tsx` | `ProfessionalCard.tsx` |
| Arquivos de rota Next.js | `kebab-case` ou `[param]` | `page.tsx`, `[slug]/page.tsx` |

---

## Formato de datas e horas

- Armazenar sempre em **UTC** no banco (`TIMESTAMPTZ`)
- Serializar na API como **ISO 8601**: `"2025-03-21T14:30:00Z"`
- Exibir na UI em horário de Brasília (UTC-3), formatado em português:
  - Data: `21 de março de 2025`
  - Data/hora: `21/03/2025 às 11:30`

---

## Formato de telefone

- Armazenar sem formatação: apenas dígitos com DDI + DDD + número
- Exemplo: `5583999998888` (55 = Brasil, 83 = João Pessoa, 9 dígitos)
- Link WhatsApp: `https://wa.me/5583999998888`
- Exibir na UI com máscara: `(83) 99999-8888`

---

## Slugs de profissionais

O slug é o identificador de URL do perfil. Gerado automaticamente a partir do nome,
categoria principal e cidade:

```
{nome-normalizado}-{categoria}-{cidade-abreviada}
```

Exemplos:
- `joao-silva-eletricista-jp` (João Pessoa)
- `maria-santos-pintura-cg` (Campina Grande)

Regras:
- Apenas letras minúsculas, números e hífens
- Sem acentos ou caracteres especiais
- Único no banco — sufixo numérico em caso de colisão: `-2`, `-3`...

---

## Paginação da API

Todas as listagens seguem o padrão cursor-based simplificado com `page` e `size`:

```json
GET /api/professionals?category=eletrica&city=joao-pessoa&page=1&size=20

{
  "data": [...],
  "pagination": {
    "page": 1,
    "size": 20,
    "total": 87,
    "totalPages": 5
  }
}
```

Valores padrão: `page=1`, `size=20`. Máximo: `size=50`.

---

## Envelope de resposta da API

**Sucesso:**
```json
{
  "data": { ... }
}
```

**Erro:**
```json
{
  "error": {
    "code": "PROFESSIONAL_NOT_FOUND",
    "message": "Profissional não encontrado"
  }
}
```

**Códigos de erro comuns:**

| Código | HTTP | Significado |
|---|---|---|
| `PROFESSIONAL_NOT_FOUND` | 404 | Profissional não existe ou inativo |
| `VALIDATION_ERROR` | 422 | Campos inválidos (detalhe em `fields`) |
| `UNAUTHORIZED` | 401 | Token ausente ou inválido |
| `FORBIDDEN` | 403 | Sem permissão para o recurso |
| `INTERNAL_ERROR` | 500 | Erro inesperado no servidor |

---

## Tipos compartilhados entre API e frontend

Como o backend é Kotlin e o frontend é TypeScript, os tipos precisam ser mantidos
em sincronia manualmente. Convenção adotada:

- Os tipos TypeScript do frontend ficam em `web/src/types/api.ts`
- Sempre que um DTO Kotlin for alterado, atualizar o tipo correspondente em `api.ts`
- Nomenclatura idêntica entre DTOs Kotlin e interfaces TypeScript (ambos em `PascalCase`)

Exemplo:

```kotlin
// api/src/main/kotlin/dto/ProfessionalDto.kt
data class ProfessionalSummaryDto(
    val id: String,
    val name: String,
    val slug: String,
    val avatarUrl: String?,
    val city: String,
    val averageRating: Double?,
    val reviewCount: Int,
    val verified: Boolean
)
```

```typescript
// web/src/types/api.ts
export interface ProfessionalSummary {
  id: string
  name: string
  slug: string
  avatarUrl: string | null
  city: string
  averageRating: number | null
  reviewCount: number
  verified: boolean
}
```

---

## Variáveis de ambiente

### api/.env.example
```
DATABASE_URL=postgresql://user:password@host:5432/trabalho_arretado
JWT_SECRET=seu-secret-aqui
R2_ACCOUNT_ID=
R2_ACCESS_KEY_ID=
R2_SECRET_ACCESS_KEY=
R2_BUCKET_NAME=trabalho-arretado-media
R2_PUBLIC_URL=https://media.trabalhoarretado.com.br
SMTP_HOST=
SMTP_PORT=587
SMTP_USER=
SMTP_PASS=
```

### web/.env.example
```
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_SITE_URL=https://www.trabalhoarretado.com.br
```
