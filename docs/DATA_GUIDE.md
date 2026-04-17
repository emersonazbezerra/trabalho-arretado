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
| Variáveis de ambiente | `SCREAMING_SNAKE_CASE` | `DATABASE_URL`, `CLOUDINARY_API_KEY` |
| Classes Android (Compose) | `PascalCase` | `ProfessionalCard`, `HomeScreen` |
| ViewModels Android | `PascalCase` + sufixo `ViewModel` | `HomeViewModel`, `SearchViewModel` |
| Arquivos Kotlin (Android) | `PascalCase.kt` | `ProfessionalCard.kt`, `HomeScreen.kt` |

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

## Tipos compartilhados entre API e app Android

Backend (Kotlin/Ktor) e app Android (Kotlin/Compose) compartilham a mesma linguagem.
Os DTOs do backend são espelhados como `data class` no módulo Android. Convenção adotada:

- DTOs do backend ficam em `api/src/main/kotlin/dto/`
- Data classes do Android ficam em `android/app/src/main/java/.../data/remote/dto/`
- Nomenclatura idêntica entre os dois lados (ambos `PascalCase` em Kotlin)

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
    val reviewCount: Int
)
```

```kotlin
// android/.../data/remote/dto/ProfessionalSummaryDto.kt
@Serializable
data class ProfessionalSummaryDto(
    val id: String,
    val name: String,
    val slug: String,
    val avatarUrl: String? = null,
    val city: String,
    val averageRating: Double? = null,
    val reviewCount: Int
)
```

---

## Variáveis de ambiente

### api/.env.example
```
DATABASE_URL=postgresql://user:password@host:5432/trabalho_arretado
JWT_SECRET=seu-secret-aqui
CLOUDINARY_CLOUD_NAME=
CLOUDINARY_API_KEY=
CLOUDINARY_API_SECRET=
```

### android/local.properties
```
# Não commitar — adicionar ao .gitignore
API_BASE_URL=http://10.0.2.2:8080
# Em produção: API_BASE_URL=https://api.trabalhoarretado.com.br
```
