# ROADMAP.md — Roadmap de Desenvolvimento

Abordagem **MVP-first**: entregar valor real o mais rápido possível, validar com usuários reais,
escalar depois. Cada fase tem critérios claros de saída e checklists técnicos separados por subprojeto.

---

## Fase 0 — Fundação

**Objetivo:** infraestrutura mínima funcionando localmente e em produção.

### Backend

- [x] Inicializar projeto Ktor com Gradle (`build.gradle.kts`, `settings.gradle.kts`, `gradlew`)
- [x] Adicionar dependências: Ktor, Exposed, Flyway, Koin, kotlinx.serialization, HikariCP, BCrypt
- [x] Configurar plugins: `Serialization.kt`, `Database.kt`, `Authentication.kt`, `Routing.kt`
- [x] Endpoint de saúde: `GET /api/health`
- [x] Criar `api/.env.example` com todas as variáveis necessárias
- [x] Configurar ktlint
- [x] PostgreSQL provisionado no Supabase (free tier)
- [ ] Backend Ktor com deploy automático no Railway conectado ao repositório (Próximo passo)

### Android

- [x] Inicializar projeto Android no Android Studio (package `br.com.trabalhoarretado`)
- [x] Adicionar dependências: Compose, Navigation, ViewModel, Retrofit, OkHttp, DataStore, Coil, Koin (Room defer para Fase 2 — aguardando KSP compatível com Kotlin 2.2.10)
- [x] Configurar `BuildConfig` com `API_BASE_URL` lido de `local.properties`
- [x] Configurar Koin (`AppModule.kt`) e DataStore
- [x] Configurar `NavHost` em `MainActivity.kt` com grafo de navegação
- [x] Configurar `Theme.kt`, `Color.kt` e `Type.kt` com a paleta e tipografia do app

**Critério de saída:** backend respondendo em produção, app rodando no emulador com tela inicial.

---

## Fase 1 — MVP

**Objetivo:** profissional se cadastra, cliente encontra e entra em contato.

### Backend

- [x] Migration `V001__create_users.sql` (tabela `users` com role `CLIENT | PROFESSIONAL`)
- [x] `POST /auth/register` — cadastro com senha hasheada via BCrypt
- [x] `POST /auth/login` — retorna token JWT (validade 7 dias)
- [x] `GET /auth/me` — dados do usuário autenticado
- [x] Middleware JWT em todas as rotas exceto `/auth/register` e `/auth/login`
- [x] Migration `V002__create_services.sql` (tabela `services` com FK para `users`)
- [x] `GET /professionals` — listagem paginada com filtros `category`, `city`, `page`
- [x] `GET /professionals/{id}` — perfil completo com serviços e avaliações agregadas
- [x] `PUT /professionals/{id}` — edição do próprio perfil (apenas PROFESSIONAL autenticado)
- [x] `POST /services` — publicar serviço (apenas PROFESSIONAL)
- [x] `PUT /services/{id}` e `DELETE /services/{id}` — editar/remover serviço (apenas dono)
- [x] Upload de avatar via S3 Ninja (S3 local), abstraído por interface `ImageStorage`, armazena URL em `users.avatar_url` (Cloudinary substituído por endpoint S3-compatível — ver `PLAN.md`)

### Android

- [x] `SplashScreen` — verificar token no DataStore, redirecionar para Login ou Home
- [x] `LoginScreen` + `RegisterScreen` com seleção de perfil (CLIENT | PROFESSIONAL)
- [x] `AuthViewModel` com `StateFlow`, persistir token no DataStore após login
- [x] Limpar token e redirecionar para Login ao receber 401 (`AuthAuthenticator` + `AuthEvent`)
- [x] `HomeScreen` — feed com profissionais em destaque e grid de categorias
- [x] `SearchScreen` — barra de busca + chips de filtro (categoria, cidade) com paginação
- [x] `ProfessionalCard` — componente reutilizável (foto, nome, categoria, cidade, nota)
- [x] `ProfessionalProfileScreen` — foto, bio, lista de serviços, botão WhatsApp
- [x] Botão favoritar (ícone de coração) — chama `POST/DELETE /favorites/{profId}`
- [ ] `PublishServiceScreen` — formulário para cadastrar/editar serviço (apenas PROFESSIONAL) (Próximo passo — Fatia 4)
- [x] `FavoritesScreen` — lista de profissionais favoritados
- [ ] `MyProfileScreen` — exibir e editar dados pessoais, upload de avatar (Próximo passo — Fatia 4)

**Critério de saída:** 10 profissionais reais cadastrados em João Pessoa, primeiros contatos registrados.

---

## Fase 2 — Avaliações e Confiança

**Objetivo:** construir o sistema de reputação que diferencia a plataforma.

### Backend

- [x] Migration `V003__create_favorites.sql` (unique constraint `client_id` + `professional_id`) — antecipado para a Fase 1
- [x] `GET /favorites`, `POST /favorites/{profId}`, `DELETE /favorites/{profId}` — antecipado para a Fase 1
- [ ] Migration `V004__create_reviews.sql` (unique constraint `client_id` + `professional_id`)
- [ ] `GET /professionals/{id}/reviews`
- [ ] `POST /professionals/{id}/reviews` — rating 1–5 + comentário
- [ ] Retornar `averageRating` e `reviewCount` no endpoint de perfil

### Android

- [ ] `ReviewList` — componente com estrelas e comentário em `ProfessionalProfileScreen`
- [ ] Formulário para submeter avaliação
- [ ] Notificação push ao profissional ao receber nova avaliação
- [ ] Estados de loading (shimmer), erro (retry) e vazio (empty state) em todas as telas
- [ ] Cache local com Room para listagens (funcionar offline após primeiro acesso)

**Critério de saída:** profissionais com 3+ avaliações reais, pelo menos 5 verificados.

---

## Fase 3 — Monetização

**Objetivo:** primeira receita recorrente.

- [ ] Modelo freemium: limite de funcionalidades no plano gratuito
- [ ] Plano Premium: destaque na busca, fotos ilimitadas, estatísticas de visualização
- [ ] Integração de pagamento (cartão + PIX recorrente)
- [ ] Dashboard do profissional: visualizações de perfil e leads recebidos
- [ ] Destaque pago (`boost`) por 7 ou 30 dias

**Critério de saída:** primeiros R$ 500/mês de receita recorrente.

---

## Fase 4 — Escala Regional

**Objetivo:** expansão para outras cidades paraibanas e nordestinas.

- [ ] Suporte a múltiplos estados (PE, RN, CE, MA)
- [ ] Busca por geolocalização (profissionais próximos ao usuário)
- [ ] Parcerias com SENAI/SEBRAE para cadastro em massa
- [ ] Programa de indicação: profissional indica colega e ganha benefício no Premium
- [ ] Publicação na Google Play Store (produção)

---

## Qualidade (contínuo)

Itens a cumprir antes de considerar cada fase encerrada:

### Backend

- [ ] Testes unitários dos services com `kotlin.test` + `mockk`
- [ ] Testes de integração com Testcontainers (PostgreSQL real)
- [ ] Relatório de cobertura com Kover (`./gradlew koverReport`)
- [ ] Validar todos os endpoints via Postman/Insomnia antes de liberar para o app

### Android

- [ ] Testes unitários dos ViewModels com `kotlin.test` + `mockk`
- [ ] Testes de UI com Compose Testing (`createComposeRule`)
- [ ] Lint sem erros (`./gradlew lint`)
- [ ] ProGuard/R8 configurado para build de release

---

## Backlog (sem prazo definido)

- Agendamento integrado (calendário no perfil do profissional)
- Chat in-app entre cliente e profissional
- Matching por IA (ranking preditivo por histórico + avaliações)
- Versão iOS (Kotlin Multiplatform ou Flutter — apenas após validação do modelo)
- API pública para integrações com construtoras e imobiliárias

---

## Notas

- Cada fase do backend deve estar concluída antes de implementar a fase correspondente no Android
- Testar no emulador com `API_BASE_URL=http://10.0.2.2:8080` (localhost da máquina host)
- Itens não planejados que surgirem devem ir para o backlog antes de serem implementados
