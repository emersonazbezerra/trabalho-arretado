# CLAUDE.md — api/ (Backend Kotlin/Ktor)

Este é o backend do **Trabalho Arretado**. Leia também `../docs/DOMAIN.md` e
`../docs/DATA_GUIDE.md` antes de qualquer tarefa de modelagem ou desenvolvimento.

---

## Stack

| Tecnologia | Versão | Papel |
|---|---|---|
| Kotlin | 2.x | Linguagem principal |
| Ktor | 3.x | Framework web |
| Exposed | 0.5x | ORM / DSL de banco |
| PostgreSQL | 16 | Banco de dados principal |
| Flyway | 10.x | Migrations de banco |
| Koin | 4.x | Injeção de dependência |
| kotlinx.serialization | — | Serialização JSON |
| HikariCP | — | Connection pool |

---

## Estrutura de pacotes

```
api/src/main/kotlin/
├── Application.kt              ← entry point, configuração do Ktor
├── plugins/
│   ├── Routing.kt              ← registra todas as rotas
│   ├── Serialization.kt        ← configura kotlinx.serialization
│   ├── Authentication.kt       ← JWT config
│   └── Database.kt             ← inicialização do Exposed + HikariCP
├── domain/
│   ├── professional/
│   │   ├── Professional.kt     ← entidade
│   │   ├── ProfessionalRepo.kt ← interface do repositório
│   │   └── ProfessionalService.kt
│   ├── review/
│   ├── lead/
│   └── ...                     ← um pacote por agregado do domínio
├── infra/
│   ├── db/
│   │   ├── tables/             ← definições de tabelas Exposed
│   │   └── migrations/        ← arquivos SQL do Flyway (V1__, V2__...)
│   └── storage/                ← integração com Cloudflare R2
├── routes/
│   ├── ProfessionalRoutes.kt
│   ├── ReviewRoutes.kt
│   └── ...
└── dto/
    ├── ProfessionalDto.kt
    └── ...
```

---

## Comandos Gradle

```bash
# Rodar o servidor em modo desenvolvimento (hot reload)
./gradlew run

# Build para produção
./gradlew buildFatJar

# Rodar testes
./gradlew test

# Rodar testes com relatório de cobertura
./gradlew koverReport

# Verificar estilo de código
./gradlew ktlintCheck

# Aplicar correções automáticas de estilo
./gradlew ktlintFormat

# Gerar classes a partir do schema (se usar geração de código)
./gradlew generateCode
```

---

## Convenções de código Kotlin

- **Estilo:** seguir o [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html) + ktlint
- **Coroutines:** preferir `suspend fun` a callbacks; nunca usar `runBlocking` fora de testes
- **Nulabilidade:** evitar `!!` — usar `?: throw` com mensagem descritiva ou `?: return`
- **Data classes:** usar para DTOs e entidades; evitar lógica de negócio dentro delas
- **Sealed classes:** preferir para modelar estados e resultados de operações (`Result<T, E>`)
- **Extension functions:** usar com moderação; nomear de forma que o contexto fique claro

### Estrutura de um route handler

```kotlin
// routes/ProfessionalRoutes.kt
fun Route.professionalRoutes(service: ProfessionalService) {
    route("/api/professionals") {
        get {
            val category = call.request.queryParameters["category"]
            val city = call.request.queryParameters["city"]
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val result = service.search(category, city, page)
            call.respond(HttpStatusCode.OK, mapOf("data" to result))
        }

        get("/{slug}") {
            val slug = call.parameters["slug"]
                ?: return@get call.respond(HttpStatusCode.BadRequest)
            val professional = service.findBySlug(slug)
                ?: return@get call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to mapOf("code" to "PROFESSIONAL_NOT_FOUND"))
                )
            call.respond(HttpStatusCode.OK, mapOf("data" to professional))
        }
    }
}
```

---

## Migrations (Flyway)

Arquivos em `src/main/resources/db/migration/`, nomenclatura obrigatória:

```
V1__create_professionals.sql
V2__create_service_categories.sql
V3__create_portfolio_items.sql
```

- Nunca editar uma migration já aplicada — criar uma nova `V{n}__alter_...`
- Sempre incluir `IF NOT EXISTS` em `CREATE TABLE`
- Usar `TIMESTAMPTZ` para todas as colunas de data/hora (armazenamento UTC)

---

## Tabelas Exposed — exemplo de referência

```kotlin
// infra/db/tables/Professionals.kt
object Professionals : UUIDTable("professionals") {
    val name = varchar("name", 255)
    val phone = varchar("phone", 20)
    val email = varchar("email", 255).uniqueIndex()
    val bio = text("bio").nullable()
    val avatarUrl = varchar("avatar_url", 500).nullable()
    val city = varchar("city", 100)
    val state = varchar("state", 2).default("PB")
    val active = bool("active").default(true)
    val createdAt = timestamp("created_at")
}
```

---

## Variáveis de ambiente necessárias

Ver `../docs/DATA_GUIDE.md` — seção "Variáveis de ambiente / api/.env.example".

Arquivo local: `api/.env` (nunca commitar — está no .gitignore).

---

## Testes

- Testes unitários em `src/test/kotlin/`
- Usar `kotlin.test` + `mockk` para mocks
- Testes de integração com banco: usar Testcontainers com PostgreSQL
- Nomenclatura: `NomeDaClasse_comportamento_resultadoEsperado()`

```kotlin
@Test
fun `ProfessionalService search returns empty list when no professionals match`() { ... }
```
