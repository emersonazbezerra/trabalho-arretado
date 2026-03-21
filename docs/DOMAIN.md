# DOMAIN.md — Modelo de Domínio

Este arquivo define as entidades principais, seus relacionamentos e a linguagem ubíqua
do Trabalho Arretado. Toda nomenclatura de código (classes, tabelas, campos, rotas) deve
seguir os termos em inglês listados aqui. A UI usa os termos em português.

---

## Linguagem ubíqua

| Termo no domínio (PT) | Termo no código (EN) | Descrição |
|---|---|---|
| Profissional | `Professional` | Trabalhador autônomo que oferece serviços |
| Cliente | `Client` | Pessoa que contrata ou busca serviços |
| Serviço / Categoria | `ServiceCategory` | Tipo de serviço oferecido (elétrica, pintura…) |
| Oferta de Serviço | `ServiceOffer` | Serviço específico que um profissional oferece |
| Portfólio | `Portfolio` | Conjunto de fotos/trabalhos do profissional |
| Item de Portfólio | `PortfolioItem` | Uma foto com título e descrição |
| Avaliação | `Review` | Nota e comentário deixado por um cliente |
| Contato / Lead | `Lead` | Evento de contato iniciado pelo cliente via WhatsApp |
| Área de Atendimento | `ServiceArea` | Bairros/cidades onde o profissional atende |
| Verificação | `Verification` | Status de checagem de identidade/qualificação |
| Selo | `Badge` | Indicador visual de verificação ou destaque no perfil |
| Plano | `Plan` | Nível de assinatura do profissional (free/premium) |

---

## Entidades principais

### Professional
Ator central da plataforma. Possui perfil público, oferece serviços e recebe avaliações.

```
Professional
├── id: UUID
├── name: String
├── phone: String          ← usado para contato WhatsApp
├── email: String
├── bio: String
├── avatarUrl: String?
├── city: String
├── state: String          ← padrão: "PB"
├── serviceAreas: List<ServiceArea>
├── serviceOffers: List<ServiceOffer>
├── portfolio: List<PortfolioItem>
├── reviews: List<Review>
├── verification: Verification
├── plan: Plan
├── active: Boolean
└── createdAt: Instant
```

### Client
Usuário que busca e contrata profissionais. Pode deixar avaliações.

```
Client
├── id: UUID
├── name: String
├── phone: String
├── email: String?
├── createdAt: Instant
└── leads: List<Lead>
```

### ServiceCategory
Taxonomia de tipos de serviço. Gerenciada pelo sistema (não pelo usuário).

```
ServiceCategory
├── id: UUID
├── slug: String           ← ex: "eletrica", "pintura", "marcenaria"
├── label: String          ← ex: "Elétrica", "Pintura", "Marcenaria"
├── iconUrl: String?
└── active: Boolean
```

Categorias iniciais: `eletrica`, `hidraulica`, `marcenaria`, `pintura`, `alvenaria`,
`limpeza`, `jardinagem`, `climatizacao`, `serralheria`, `informatica`

### ServiceOffer
Serviço específico que um profissional declara oferecer.

```
ServiceOffer
├── id: UUID
├── professionalId: UUID
├── categoryId: UUID
├── description: String?
└── priceRange: String?    ← ex: "A partir de R$ 150"
```

### PortfolioItem
Foto de trabalho realizado, com contexto.

```
PortfolioItem
├── id: UUID
├── professionalId: UUID
├── imageUrl: String
├── title: String?
├── description: String?
└── createdAt: Instant
```

### Review
Avaliação deixada por um cliente após um serviço.

```
Review
├── id: UUID
├── professionalId: UUID
├── clientId: UUID?        ← nullable: reviews importadas do Google não têm client local
├── rating: Int            ← 1 a 5
├── comment: String?
├── source: ReviewSource   ← INTERNAL | GOOGLE
├── verified: Boolean
└── createdAt: Instant
```

### Lead
Registro de contato iniciado pelo cliente (clique no botão WhatsApp).

```
Lead
├── id: UUID
├── professionalId: UUID
├── clientId: UUID?
├── categoryId: UUID?
├── utmSource: String?
└── createdAt: Instant
```

### Verification
Status de verificação do profissional.

```
Verification
├── id: UUID
├── professionalId: UUID
├── status: VerificationStatus   ← PENDING | VERIFIED | REJECTED
├── checkedAt: Instant?
└── notes: String?
```

### Plan
Plano de assinatura do profissional.

```
Plan
├── FREE      ← perfil básico, portfólio limitado a 3 fotos
└── PREMIUM   ← destaque na busca, portfólio ilimitado, estatísticas de visualização
```

---

## Regras de negócio essenciais

- Um `Professional` só aparece nas buscas se `active = true`
- O campo `phone` do profissional é a âncora do link WhatsApp (`wa.me/55<phone>`)
- `Review` com `source = GOOGLE` são sincronizadas via Google Business API e não podem ser editadas internamente
- Um `Lead` é registrado no momento do clique no botão WhatsApp, antes do redirecionamento
- Profissionais no plano `FREE` aparecem após os `PREMIUM` no ranking de busca por categoria/cidade
- `PortfolioItem`: máximo 3 itens no plano FREE, ilimitado no PREMIUM

---

## Relacionamentos

```
Professional  1──n  ServiceOffer
Professional  1──n  PortfolioItem
Professional  1──n  Review
Professional  1──1  Verification
Professional  1──1  Plan
Professional  1──n  Lead
ServiceOffer  n──1  ServiceCategory
Review        n──1  Client (opcional)
Lead          n──1  Client (opcional)
```
