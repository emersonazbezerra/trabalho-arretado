# DECISIONS.md — Registro de Decisões Técnicas (ADRs)

Cada decisão relevante de arquitetura ou tecnologia é documentada aqui no formato ADR
(Architecture Decision Record). O objetivo é preservar o raciocínio por trás das escolhas,
especialmente para revisitá-las no futuro.

Formato: **status** → `Aceito` | `Proposto` | `Substituído` | `Depreciado`

---

## ADR-001 — Monorepo com api/ e android/ no mesmo repositório

**Status:** Aceito
**Data:** 2025-03

**Contexto:**
Projeto desenvolvido por uma única pessoa. A separação em múltiplos repositórios adicionaria
overhead de sincronização sem benefício real na fase inicial.

**Decisão:**
Manter backend (api/) e app Android (android/) no mesmo repositório Git, com deploys independentes.

**Consequências:**
+ Mudanças que afetam as duas camadas ficam em um único commit/PR
+ Contexto unificado para o Claude Code
— Builds do CI precisam detectar qual subprojeto mudou para evitar redeploy desnecessário

---

## ADR-002 — Backend: Kotlin + Ktor (não Spring Boot)

**Status:** Aceito  
**Data:** 2025-03

**Contexto:**  
O desenvolvedor já tem familiaridade com Kotlin. A escolha era entre Ktor e Spring Boot.

**Decisão:**  
Usar Ktor 3.x como framework principal do backend.

**Justificativa:**  
- Footprint de memória significativamente menor que Spring Boot (relevante em infra barata)
- Coroutines Kotlin como cidadão de primeira classe, sem adaptadores
- Startup mais rápido para deploys em Railway/Render com free tier
- Ausência de magia implícita — comportamento explícito e previsível

**Consequências:**  
+ Menor custo de infra no início  
+ Código mais idiomático em Kotlin puro  
— Ecossistema menor de plugins prontos vs Spring  
— Menos exemplos e documentação na comunidade brasileira

---

## ADR-003 — Cliente: App Android nativo (Kotlin + Jetpack Compose)

**Status:** Aceito
**Data:** 2025-04

**Contexto:**
O público-alvo — profissionais autônomos e clientes no Nordeste — acessa majoritariamente
por dispositivos Android. A abordagem nativa garante melhor experiência em hardware de médio/baixo
custo, acesso a APIs do dispositivo e distribuição via Play Store.

**Decisão:**
App Android nativo com Kotlin e Jetpack Compose, arquitetura MVVM.

**Justificativa:**
- Kotlin é a linguagem oficial recomendada pelo Google para Android
- Jetpack Compose reduz código de UI em relação à abordagem XML e é o padrão atual do Android
- MVVM com StateFlow é o padrão recomendado pelo Google Architecture Guidelines
- Mesma linguagem do backend (Kotlin) — curva de aprendizado reduzida

**Consequências:**
+ Experiência nativa otimizada para Android
+ Coroutines + StateFlow como cidadãos de primeira classe
— Requer Android Studio e emulador/dispositivo para desenvolvimento
— iOS não coberto (fora do escopo atual)

---

## ADR-004 — Banco de dados: PostgreSQL no Supabase

**Status:** Aceito
**Data:** 2025-04

**Contexto:**
Necessidade de dados relacionais bem estruturados com o menor custo possível no MVP.

**Decisão:**
PostgreSQL hospedado no Supabase (free tier). ORM via Exposed (Kotlin). Migrations via Flyway.

**Justificativa:**
- Supabase oferece PostgreSQL gerenciado com free tier (500MB, 2 projetos) — custo zero no MVP
- Exposed é idiomático em Kotlin e evita o overhead do Hibernate
- Conexão via `DATABASE_URL` padrão — sem acoplamento ao Supabase além da string de conexão
- Fácil migrar para Railway ou outro provedor no futuro, só trocando a variável de ambiente

**Consequências:**
+ Custo zero no MVP
+ Tipagem segura via Exposed DSL
— Limite de 500MB e 2 projetos no free tier — monitorar conforme a base cresce
— Migrations manuais via Flyway (sem geração automática de schema)

---

## ADR-005 — Estratégia de autenticação: e-mail + senha com JWT

**Status:** Aceito
**Data:** 2025-04

**Contexto:**
Tanto clientes quanto profissionais precisam de conta para acessar o app. A autenticação
deve funcionar offline após o primeiro login (token cacheado no dispositivo).

**Decisão:**
Autenticação por e-mail e senha. Senhas armazenadas com BCrypt. Token JWT com validade de
7 dias, persistido no DataStore do Android e enviado automaticamente pelo Retrofit.

**Justificativa:**
- Funciona sem dependência de serviço externo de e-mail/SMS
- JWT stateless simplifica o backend (sem sessões)
- DataStore é a solução recomendada pelo Google para persistência de credenciais no Android
- BCrypt é padrão da indústria para hash de senhas

**Consequências:**
+ Autenticação funciona mesmo com conectividade intermitente (token cacheado)
+ Sem dependência de entregabilidade de e-mail para login
— Refresh token não implementado no MVP — usuário precisa logar novamente após 7 dias
— Revisitar para adicionar login social (Google) futuramente

---

## ADR-006 — Armazenamento de imagens: Cloudinary

**Status:** Aceito
**Data:** 2025-04

**Contexto:**
Fotos de perfil de profissionais precisam ser uploadadas pelo app Android e servidas com boa
performance. O SDK mobile é fator relevante.

**Decisão:**
Usar Cloudinary para upload e armazenamento das fotos de perfil.

**Justificativa:**
- SDK Android oficial disponível, simplificando o upload direto do app
- Transformações de imagem (resize, crop, compressão) via URL sem processamento no backend
- Free tier de 25 créditos/mês suficiente para o início
- URL pública estável para cada imagem (usada como `avatar_url` na tabela `users`)

**Consequências:**
+ Upload direto do app sem passar pelo backend
+ Transformações de imagem on-the-fly gratuitas
— Vendor lock-in maior que R2 (API proprietária)
— Monitorar uso de créditos conforme base de usuários cresce

---

## Como adicionar uma nova decisão

Copie o template abaixo e incremente o número:

```markdown
## ADR-00X — Título da decisão

**Status:** Proposto  
**Data:** YYYY-MM

**Contexto:**  
[Qual problema ou situação motivou essa decisão?]

**Decisão:**  
[O que foi decidido?]

**Justificativa:**  
[Por que essa opção e não outras?]

**Consequências:**  
+ [Benefícios]  
— [Trade-offs e riscos]
```
