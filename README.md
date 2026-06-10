# Trabalho Arretado

> Plataforma de divulgação e conexão de profissionais autônomos no Nordeste brasileiro.

---

## O que é

O Trabalho Arretado conecta quem precisa de um serviço — reforma, elétrica, hidráulica, marcenaria, pintura e mais — com profissionais autônomos verificados na Paraíba e no Nordeste. A plataforma digitaliza o boca-a-boca regional, oferecendo perfis com portfólio visual, avaliações e contato direto via WhatsApp.

## Estrutura do repositório

```
trabalho-arretado/
├── api/          ← Backend: Kotlin + Ktor
├── android/      ← App Android: Kotlin + Jetpack Compose
└── docs/         ← Documentação de produto, domínio e decisões técnicas
```

> Cada subprojeto tem seu próprio `CLAUDE.md` com convenções específicas (`api/CLAUDE.md`, `android/CLAUDE.md`).

## Stack

### Backend (`api/`)

- **Kotlin** + **Ktor** (servidor Netty)
- **Exposed** (ORM) sobre **PostgreSQL**, com **HikariCP** e migrações via **Flyway**
- **Koin** para injeção de dependência
- Autenticação via **JWT** (auth0 java-jwt) + **BCrypt** para senhas
- Upload de imagens via **AWS S3 SDK** (compatível com S3 Ninja em desenvolvimento local)
- **ktlint** para padronização de código
- JDK 21

### Android (`android/`)

- **Kotlin** + **Jetpack Compose** (Material 3)
- **Jetpack Navigation Compose**
- **MVVM**: `ViewModel` + `StateFlow` (`UiState` selado por tela)
- **Retrofit** + **OkHttp** + **kotlinx.serialization** para consumo da API
- **DataStore (Preferences)** para armazenamento do token JWT
- **Coil** para carregamento de imagens
- **Koin** para injeção de dependência

## Arquitetura

### Backend — arquitetura em camadas

```
api/src/main/kotlin/br/com/trabalhoarretado/
├── domain/        ← entidades, interfaces de repositório, exceções de domínio
├── application/   ← serviços de aplicação, DTOs de request/response
├── infra/         ← implementações de repositório (Exposed) e storage de imagens (S3)
├── routes/        ← rotas Ktor
├── plugins/       ← plugins Ktor (roteamento, tratamento de erros)
└── di/            ← módulo Koin com a injeção de dependências
```

Fluxo: `routes` → `application` (services) → `domain` (regras e contratos) ← `infra` (implementações).

### Android — MVVM

```
android/app/src/main/java/br/com/trabalhoarretado/
├── data/
│   ├── dto/           ← data classes de request/response JSON
│   ├── remote/        ← ApiService (Retrofit) e interceptors
│   ├── local/          ← DataStore (TokenStore)
│   └── repository/    ← repositórios (Auth, Professional, Favorites, Review, User)
├── domain/             ← wrapper Result e helpers de chamada de API
├── di/                 ← módulo Koin
└── ui/
    ├── theme/          ← cores, tipografia, MaterialTheme
    ├── splash/         ← tela de abertura
    ├── auth/           ← login e cadastro
    ├── home/           ← feed de profissionais em destaque e categorias
    ├── search/         ← busca com filtros
    ├── professional/   ← perfil do profissional e publicação de serviço
    ├── favorites/       ← lista de favoritos
    ├── profile/         ← meu perfil (edição, troca de role para profissional)
    ├── common/          ← componentes compartilhados (cards, dropdowns, categorias)
    └── navigation/      ← NavHost e rotas
```

Fluxo: UI (Composables) → ViewModel (`StateFlow` + `UiState`) → Repository → `ApiService` (Retrofit).

## Requisitos

- **Backend:** JDK 21+, Gradle 8+, PostgreSQL, Docker (para S3 Ninja em dev)
- **Android:** Android Studio (latest stable), JDK 21, emulador ou dispositivo Android (minSdk 26)

## Rodando localmente

```bash
# Clonar o repositório
git clone <repo-url>
cd trabalho-arretado

# Backend
cd api
cp .env.example .env   # preencher variáveis
./gradlew run
```

A API estará disponível em `http://localhost:8080`.

Para o app Android, abra a pasta `android/` no Android Studio e execute via IDE, ou via linha de comando com um emulador já iniciado:

```bash
cd android
./gradlew installDebug
```

Configure `android/local.properties` com `API_BASE_URL=http://10.0.2.2:8080` (o emulador acessa o `localhost` da máquina host por esse endereço).

## Documentação

Consulte a pasta `docs/` para contexto de produto (`CONTEXT.md`), modelagem de domínio (`DOMAIN.md`), roadmap (`ROADMAP.md`), decisões técnicas (`DECISIONS.md`) e convenções de dados (`DATA_GUIDE.md`).

## Licença

Privado — todos os direitos reservados.
