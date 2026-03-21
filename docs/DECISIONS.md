# DECISIONS.md — Registro de Decisões Técnicas (ADRs)

Cada decisão relevante de arquitetura ou tecnologia é documentada aqui no formato ADR
(Architecture Decision Record). O objetivo é preservar o raciocínio por trás das escolhas,
especialmente para revisitá-las no futuro.

Formato: **status** → `Aceito` | `Proposto` | `Substituído` | `Depreciado`

---

## ADR-001 — Monorepo com api/ e web/ no mesmo repositório

**Status:** Aceito  
**Data:** 2025-03

**Contexto:**  
Projeto desenvolvido por uma única pessoa. A separação em múltiplos repositórios adicionaria
overhead de sincronização sem benefício real na fase inicial.

**Decisão:**  
Manter backend (api/) e frontend (web/) no mesmo repositório Git, com deploys independentes.

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

## ADR-003 — Frontend: Next.js + Tailwind CSS + shadcn/ui

**Status:** Aceito  
**Data:** 2025-03

**Contexto:**  
A plataforma depende fortemente de SEO para páginas de perfil ("eletricista em João Pessoa").
O público acessa majoritariamente pelo mobile, sem app instalado.

**Decisão:**  
Next.js com SSR/SSG para perfis, Tailwind CSS para estilização e shadcn/ui para componentes.

**Justificativa:**  
- SSR nativo garante indexação dos perfis por buscadores
- PWA via Next.js elimina necessidade de app nativo no MVP
- shadcn/ui: componentes copiados no projeto, sem lock-in de biblioteca
- Deploy gratuito na Vercel com CI/CD automático

**Consequências:**  
+ SEO otimizado desde o início  
+ Deploy zero-config na Vercel  
— TypeScript no front, Kotlin no backend — tipos compartilhados precisam ser mantidos manualmente (ver DATA_GUIDE.md)

---

## ADR-004 — Banco de dados: PostgreSQL

**Status:** Aceito  
**Data:** 2025-03

**Contexto:**  
Necessidade de queries geoespaciais (busca por raio) e dados relacionais bem estruturados.

**Decisão:**  
PostgreSQL com extensão PostGIS para geolocalização. ORM via Exposed (Kotlin).

**Justificativa:**  
- PostGIS resolve busca por raio sem serviço externo
- Exposed é idiomático em Kotlin e evita o overhead do Hibernate
- Supabase oferece PostgreSQL gerenciado com free tier generoso para o início

**Consequências:**  
+ Queries geoespaciais nativas quando necessário  
+ Tipagem segura via Exposed DSL  
— Migrations manuais via Flyway (sem geração automática de schema)

---

## ADR-005 — Estratégia de autenticação: magic link por e-mail (profissional)

**Status:** Proposto  
**Data:** 2025-03

**Contexto:**  
Profissionais autônomos no Nordeste têm baixa familiaridade com senhas seguras. O fluxo de
"esqueci minha senha" é fonte comum de abandono em cadastros.

**Decisão:**  
Autenticação de profissionais via magic link enviado para e-mail (ou OTP por SMS como fallback).
Clientes não criam conta no MVP — apenas identificação opcional por telefone.

**Justificativa:**  
- Reduz fricção no cadastro  
- Elimina gestão de senhas na fase inicial  
- SMS OTP via Twilio ou Zenvia como fallback para quem não tem e-mail

**Consequências:**  
+ Menos abandono no onboarding  
— Dependência de entregabilidade de e-mail/SMS  
— Revisitar quando houver necessidade de login social (Google)

---

## ADR-006 — Armazenamento de imagens: Cloudflare R2

**Status:** Proposto  
**Data:** 2025-03

**Contexto:**  
Portfólio de profissionais exige upload e servição de imagens com boa performance e custo baixo.

**Decisão:**  
Usar Cloudflare R2 para armazenamento de imagens (portfólio e avatars).

**Justificativa:**  
- R2 tem free tier de 10GB e zero egress fee (diferencial importante vs S3)
- CDN da Cloudflare embutida sem configuração adicional
- API compatível com S3 — fácil de trocar se necessário

**Consequências:**  
+ Custo próximo de zero no início  
+ Imagens servidas via CDN global  
— Vendor lock-in leve (mitigado pela compatibilidade S3)

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
