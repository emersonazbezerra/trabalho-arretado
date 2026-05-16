# Plano — Fase 1 Android (iterativo) + pré-requisitos de backend

## Contexto

O backend já cobre **autenticação, profissionais e serviços** (Fase 1 do roadmap). O Android está em scaffolding: Compose, Navigation, Koin (módulo vazio), tema, `BuildConfig.API_BASE_URL` e uma `SplashScreen` placeholder. Nenhum Retrofit/DataStore/ViewModel implementado.

Conforme a nota do roadmap ("cada fase do backend deve estar concluída antes do Android"), e a decisão do usuário:

1. **Antecipar favoritos do backend** (originalmente Fase 2) para que o botão de favoritar em `ProfessionalProfileScreen` e a `FavoritesScreen` da Fase 1 Android funcionem de verdade.
2. **Substituir Cloudinary por S3 Ninja local** (S3 mock da scireum) via `docker-compose` para focar em desenvolvimento local. MinIO foi arquivado em abril/2026; S3 Ninja é um mock leve com UI web nativa (porta 9000), zero bootstrap (buckets criados sob demanda) — ótimo para dev. O contrato de upload de avatar fica abstraído por uma interface `ImageStorage`, permitindo trocar para S3 real/Cloudinary depois só mudando envs.

O resultado: ao final, o app Android entrega o fluxo completo da Fase 1 — cadastro/login, busca, perfil, publicação de serviço, favoritos, perfil próprio com avatar.

---

## Pré-requisitos no backend (antes do Android)

### P0. S3 Ninja (S3 local) no `docker-compose.yml` — ✅ feito
- Serviço `s3` (imagem `docker.io/scireum/s3-ninja:latest`) na porta `9000`, volume `s3_data`.
- UI web em `http://localhost:9000/`; endpoint S3 em `http://localhost:9000/s3` (path-style).
- `api/.env.example` atualizado com `S3_ENDPOINT`, `S3_REGION`, `S3_ACCESS_KEY`, `S3_SECRET_KEY` (AWS example), `S3_BUCKET=avatars`, `S3_PUBLIC_BASE_URL=http://localhost:9000/s3/avatars`, `S3_PATH_STYLE=true`. Bucket `avatars` será criado sob demanda no boot do app (sem script de bootstrap).

### P1. Storage abstraído + upload de avatar
- **Novo:** `api/src/main/kotlin/br/com/trabalhoarretado/infra/storage/ImageStorage.kt` — interface `suspend fun upload(bytes: ByteArray, contentType: String, key: String): String` (retorna URL pública).
- **Novo:** `infra/storage/S3ImageStorage.kt` — implementação usando AWS SDK v2 S3 client (`software.amazon.awssdk:s3`) com `forcePathStyle(true)` apontando para o endpoint S3 Ninja. A mesma classe servirá depois para S3 real / Cloudinary-S3-compat trocando apenas envs. Adicionar dependência no `api/build.gradle.kts`.
- **Registrar no Koin** (`api/src/main/kotlin/.../di/`).
- **Nova rota:** `POST /api/users/me/avatar` (multipart) — recebe imagem (validar `image/jpeg|png|webp`, ≤ 2 MB), gera `key = "users/{userId}/{uuid}.{ext}"`, persiste `avatarUrl` em `users`, retorna `UserResponse`. Adicionar em `routes/AuthRoutes.kt` ou novo `UserRoutes.kt`.

### P2. Favoritos (antecipado da Fase 2)
- **Migration:** `api/src/main/resources/db/migration/V003__create_favorites.sql` — tabela `favorites` com `client_id`/`professional_id` (ambos FK para `users`), `created_at TIMESTAMPTZ`, unique `(client_id, professional_id)`.
- **Tabela Exposed:** `infra/db/tables/Favorites.kt`.
- **Domínio:** `domain/favorite/Favorite.kt`, `FavoriteRepo.kt` (interface) + `FavoriteRepoImpl.kt` no `infra/db/repo/`, `FavoriteService.kt`.
- **Rotas:** `routes/FavoriteRoutes.kt`
  - `GET /api/favorites` → lista `ProfessionalSummaryResponse[]` favoritados pelo cliente autenticado.
  - `POST /api/favorites/{profId}` → 201, idempotente (retornar 200 se já existe).
  - `DELETE /api/favorites/{profId}` → 204.
  - Apenas role `CLIENT`. Validar que `profId` é de um `PROFESSIONAL` existente.
- Registrar em `plugins/Routing.kt` e no módulo Koin.

### P3. Testes & verificação backend
- Testes unitários de `FavoriteService` (kotlin.test + mockk).
- Smoke manual via arquivos `.http` existentes em `api/` (já há `auth.http`, `professionals.http`); adicionar `favorites.http` e `avatar.http`.

---

## Android — Fase 1 em fatias iterativas

Cada fatia é **independentemente testável no emulador** (`API_BASE_URL=http://10.0.2.2:8080`). Convenções fixadas em `android/CLAUDE.md` valem para todas: MVVM com `StateFlow + sealed UiState`, injeção via `koinViewModel()`, kotlinx.serialization, package `br.com.trabalhoarretado`.

### Infraestrutura compartilhada (base de todas as fatias)

Construído na Fatia 1, reusado depois.

- **Pacotes a criar** sob `app/src/main/java/br/com/trabalhoarretado/`:
  - `data/remote/` — `ApiService.kt`, `AuthInterceptor.kt`, `AuthAuthenticator.kt`, `NetworkModule.kt` (factory de Retrofit/OkHttp/JSON).
  - `data/local/` — `TokenStore.kt` (DataStore Preferences, chave `auth_token`).
  - `data/dto/` — DTOs espelhando exatamente os contratos da API (camelCase). Ver lista abaixo.
  - `data/repository/` — `AuthRepository`, `ProfessionalRepository`, `ServiceRepository`, `FavoriteRepository`, `UserRepository`.
  - `domain/` — `Result.kt` (sealed: `Success<T>` / `Error(code, message, httpStatus)`), `AuthEvent.kt` (SharedFlow para 401).
  - `ui/common/` — `UiState.kt` (`Loading | Success<T> | Empty | Error`), componentes reutilizáveis (`ProfessionalCard`, `LoadingShimmer` simplificado, `ErrorRetry`).

- **DTOs (data/dto):**
  - `LoginRequest`, `RegisterRequest`, `AuthResponse`, `UserDto`
  - `ProfessionalSummaryDto`, `ProfessionalProfileDto`, `PaginationDto`, `PaginatedProfessionalsDto`
  - `UpdateProfessionalRequest`
  - `ServiceDto`, `CreateServiceRequest`, `UpdateServiceRequest`
  - `ApiErrorDto` (`{ error: { code, message } }`)

- **Network layer:**
  - `AuthInterceptor` lê token de `TokenStore` (runBlocking apenas dentro do interceptor) e adiciona `Authorization: Bearer …`.
  - `AuthAuthenticator` (OkHttp `Authenticator`) intercepta 401 → limpa `TokenStore` → emite `AuthEvent.Unauthorized` num `MutableSharedFlow` global → retorna `null` (não tenta novo request).
  - `Result` wrapper: extension `suspend fun <T> apiCall(...): Result<T>` que envolve chamadas Retrofit, mapeia `HttpException` para `Result.Error` lendo o body como `ApiErrorDto`.
  - Registrar tudo no `di/AppModule.kt` (`single` para Retrofit/OkHttp/TokenStore/repositories; `viewModel { … }` para ViewModels).

- **Navegação:** trocar rotas string-soltas por uma `sealed class Screen` em `ui/navigation/Screen.kt` (rotas: `Splash`, `Login`, `Register`, `Home`, `Search`, `Professional(id)`, `PublishService(serviceId?)`, `Favorites`, `MyProfile`). `MainActivity` observa `AuthEvent.Unauthorized` e faz `popBackStack` para `Login`.

---

### Fatia 1 — Auth + Splash funcional

**Critério de saída:** usuário cria conta como CLIENT ou PROFESSIONAL, faz login, token persiste, reabrir o app pula direto para Home (placeholder).

- Implementar toda a infra compartilhada acima.
- `data/repository/AuthRepository.kt`: `register`, `login`, `me`, `logout` (limpa TokenStore).
- `ui/auth/AuthViewModel.kt` — `StateFlow<AuthUiState>`; ações `login(email,password)`, `register(form)`; persiste token + dispara navegação.
- `ui/auth/LoginScreen.kt` — campos email/senha, link para Register, erros inline.
- `ui/auth/RegisterScreen.kt` — formulário com `RadioGroup` para `CLIENT | PROFESSIONAL`, campos nome/email/senha/cidade/estado/telefone.
- `ui/splash/SplashScreen.kt` — usar `LaunchedEffect` para ler `TokenStore`; se token presente, chamar `GET /auth/me` (valida não-expirado) → navegar para `Home`; senão → `Login`.
- `HomeScreen.kt` mínimo (texto "Olá, {nome}" + botão Logout) — só para fechar o ciclo.
- **Verificação:** registrar via app → ver registro no Postgres; matar app, reabrir → entra direto na Home; logout → volta para Login; forçar token inválido manualmente → Authenticator dispara redirect.

### Fatia 2 — Home + Search + ProfessionalCard

**Critério de saída:** usuário lista profissionais com paginação, filtra por categoria/cidade.

- `data/repository/ProfessionalRepository.kt`: `list(category?, city?, page)`, `getById(id)`.
- `ui/common/ProfessionalCard.kt` — composable reutilizável (Coil para `avatarUrl`, nome, cidade, estrelas via `averageRating`, contagem).
- `ui/home/HomeScreen.kt` + `HomeViewModel.kt` — grid de categorias (lista hardcoded curta inicial) + seção "Destaques" carregando `GET /professionals?page=1` (ordenado pelo backend; refinar ordenação depois se necessário).
- `ui/search/SearchScreen.kt` + `SearchViewModel.kt` — `TextField` com debounce (300 ms), chips de filtro (categoria, cidade), `LazyColumn` com **paginação manual** (carregar próxima página quando chega ao fim, sem Paging 3 nesta fase para não inflar escopo).
- Estados Loading/Empty/Error com componentes comuns. Shimmer simplificado (placeholders cinza), refinamento visual fica para Fase 2.
- **Verificação:** seedar ~5 profissionais no banco; rolar lista até paginar; aplicar filtros e ver requisição mudar (Logcat com `HttpLoggingInterceptor`).

### Fatia 3 — Perfil do profissional + WhatsApp + Favoritar

**Critério de saída:** cliente abre perfil, vê serviços, clica WhatsApp, favorita/desfavorita.

- `data/repository/FavoriteRepository.kt`: `list()`, `add(profId)`, `remove(profId)`.
- `ui/professional/ProfessionalProfileScreen.kt` + `ProfessionalProfileViewModel.kt`:
  - Carrega `GET /professionals/{id}` no init.
  - Carrega favoritos do usuário (`GET /favorites`) para pintar o estado inicial do ícone (apenas se role atual = CLIENT). Cache leve no ViewModel.
  - Botão WhatsApp: `Intent.ACTION_VIEW` com `https://wa.me/{phoneE164}` (sanitizar dígitos do `phone`).
  - Ícone de coração: alterna otimisticamente, rollback em erro.
  - Lista de serviços do profissional.
- `ui/favorites/FavoritesScreen.kt` + `FavoritesViewModel.kt` — lista via `GET /favorites` reusando `ProfessionalCard`. Estado vazio com CTA.
- **Verificação:** logar como CLIENT, favoritar/desfavoritar 2-3 profissionais, abrir tela Favoritos, conferir que o estado persiste após reload; deslogar/relogar como PROFESSIONAL e confirmar que botão de favoritar fica oculto (ou desabilitado).

### Fatia 4 — Publicar serviço + MyProfile com avatar

**Critério de saída:** profissional edita perfil, troca avatar, publica/edita/remove serviço.

- `data/repository/ServiceRepository.kt`: `create`, `update`, `delete`.
- `data/repository/UserRepository.kt`: `updateProfessional(id, body)`, `uploadAvatar(byteArray, mimeType)` (multipart Retrofit `@Part MultipartBody.Part`).
- `ui/professional/PublishServiceScreen.kt` + ViewModel — modo create/edit pelo argumento `serviceId?` na rota; gate de acesso: só PROFESSIONAL.
- `ui/profile/MyProfileScreen.kt` + `MyProfileViewModel.kt`:
  - Form para nome/cidade/estado/telefone (`PUT /professionals/{me.id}` apenas para PROFESSIONAL; CLIENT vê só dados básicos somente leitura nesta fase).
  - Upload de avatar: usar `ActivityResultContracts.PickVisualMedia` (Photo Picker), redimensionar local antes do upload (ex.: `Bitmap` → JPEG quality 85, máx 1024px lado maior) para reduzir payload, enviar como multipart.
  - Após sucesso, atualizar avatar no cache do user (re-chamar `/auth/me`). URLs apontarão para `http://10.0.2.2:3900/avatars/...` em dev.
  - Botão "Meus serviços" abre lista local com swipe-to-delete e CTA para `PublishServiceScreen`.
- **Verificação:** logar como PROFESSIONAL, trocar avatar (ver URL apontando para `http://10.0.2.2:3900/avatars/...` no perfil — Coil deve carregar), publicar serviço, editar, remover; abrir o profissional como CLIENT em outra conta e confirmar que o serviço aparece.

---

## Arquivos críticos a tocar/criar

### Backend
- `api/docker-compose.yml` (editar)
- `api/.env.example` (editar)
- `api/build.gradle.kts` (adicionar AWS SDK v2 S3)
- `api/src/main/kotlin/br/com/trabalhoarretado/infra/storage/{ImageStorage,S3ImageStorage}.kt` (novo)
- `api/src/main/resources/db/migration/V003__create_favorites.sql` (novo)
- `api/src/main/kotlin/.../infra/db/tables/Favorites.kt` (novo)
- `api/src/main/kotlin/.../domain/favorite/*` (novo: entidade, repo, service)
- `api/src/main/kotlin/.../routes/FavoriteRoutes.kt` (novo)
- `api/src/main/kotlin/.../routes/AuthRoutes.kt` ou novo `UserRoutes.kt` (adicionar `POST /users/me/avatar`)
- `api/src/main/kotlin/.../plugins/Routing.kt` (registrar)
- `api/src/main/kotlin/.../di/*` (registrar Koin)

### Android (todas sob `android/app/src/main/java/br/com/trabalhoarretado/`)
- `di/AppModule.kt` (preencher)
- `data/remote/{ApiService,AuthInterceptor,AuthAuthenticator,NetworkModule}.kt`
- `data/local/TokenStore.kt`
- `data/dto/*.kt` (lista acima)
- `data/repository/{Auth,Professional,Service,Favorite,User}Repository.kt`
- `domain/{Result,AuthEvent}.kt`
- `ui/navigation/Screen.kt`
- `ui/common/{ProfessionalCard,UiState,LoadingShimmer,ErrorRetry}.kt`
- `ui/auth/{LoginScreen,RegisterScreen,AuthViewModel}.kt`
- `ui/splash/SplashScreen.kt` (reescrever)
- `ui/home/{HomeScreen,HomeViewModel}.kt`
- `ui/search/{SearchScreen,SearchViewModel}.kt`
- `ui/professional/{ProfessionalProfileScreen,ProfessionalProfileViewModel,PublishServiceScreen,PublishServiceViewModel}.kt`
- `ui/favorites/{FavoritesScreen,FavoritesViewModel}.kt`
- `ui/profile/{MyProfileScreen,MyProfileViewModel}.kt`
- `MainActivity.kt` (NavHost completo + observar `AuthEvent.Unauthorized`)

---

## Verificação fim-a-fim

1. `cd api && docker compose up -d` → Postgres + S3 Ninja de pé; API S3 do S3 Ninja em `localhost:3900`.
2. `cd api && ./gradlew run` → backend em `:8080`. Smoke via arquivos `.http` (auth, professionals, services, favorites, avatar).
3. `cd android && ./gradlew installDebug` (emulador rodando, `API_BASE_URL=http://10.0.2.2:8080` em `local.properties`).
4. Roteiro manual: registrar PROFESSIONAL → editar perfil + avatar → publicar 2 serviços. Registrar CLIENT em outra conta → buscar, abrir perfil, favoritar, abrir WhatsApp, ver Favoritos. Matar e reabrir o app em ambas as contas (token persiste). Forçar 401 (alterar token no DataStore) → Authenticator empurra para Login.
5. `./gradlew lint` no Android e `./gradlew ktlintCheck test` no backend antes de fechar cada fatia.

---

## Itens fora de escopo (ficam para Fase 2+)

- Reviews/avaliações (Fase 2 do roadmap; favoritos foi a única antecipação combinada).
- Notificações push.
- Cache offline com Room (continua bloqueado por KSP/Kotlin 2.2.10 conforme nota da Fase 0).
- Paging 3 — `LazyColumn` com paginação manual basta para a Fase 1.
- Migração futura de S3 Ninja → S3/Cloudinary — facilitada por `ImageStorage` ser interface (mesma `S3ImageStorage` serve para qualquer endpoint S3-compatible só trocando envs).
