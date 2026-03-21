# ROADMAP.md — Roadmap de Desenvolvimento

O desenvolvimento segue uma abordagem **MVP-first**: entregar valor real o mais rápido possível,
validar com usuários reais, e só então escalar features. Cada fase tem critérios claros de saída.

---

## Fase 0 — Fundação (semanas 1–2)
**Objetivo:** infraestrutura mínima funcionando localmente e em produção.

- [ ] Monorepo configurado (api/ + web/)
- [ ] Backend: projeto Ktor inicializado, health check `/api/health`
- [ ] Frontend: projeto Next.js inicializado, página em branco no ar
- [ ] Banco de dados PostgreSQL provisionado (Railway ou Supabase)
- [ ] Deploy automático: Vercel (web) + Railway (api) conectados ao repositório
- [ ] Variáveis de ambiente documentadas em `.env.example`
- [ ] Domínio `www.trabalhoarretado.com.br` apontando para Vercel

**Critério de saída:** `https://www.trabalhoarretado.com.br` no ar com página inicial estática.

---

## Fase 1 — MVP (semanas 3–10)
**Objetivo:** profissional se cadastra, cliente encontra e entra em contato.

### Backend
- [ ] Migrations com Flyway/Exposed
- [ ] CRUD de `Professional` (cadastro, edição, ativação)
- [ ] CRUD de `ServiceOffer` e `ServiceCategory` (categorias seedadas)
- [ ] Upload de imagens para portfólio (Cloudflare R2 ou Supabase Storage)
- [ ] Endpoint de busca: `/api/professionals?category=&city=&page=`
- [ ] Endpoint de perfil público: `/api/professionals/{slug}`
- [ ] Registro de `Lead` ao clicar em contato WhatsApp
- [ ] Autenticação básica para profissionais (magic link por e-mail ou SMS OTP)

### Frontend
- [ ] Página inicial com barra de busca (categoria + cidade)
- [ ] Listagem de profissionais com cards (foto, nome, categoria, cidade, nota média)
- [ ] Página de perfil do profissional (`/profissional/[slug]`)
  - Foto, bio, categorias, área de atendimento
  - Galeria de portfólio
  - Botão WhatsApp com rastreamento de lead
  - Avaliações
- [ ] Formulário de cadastro para profissionais (`/cadastrar`)
- [ ] SEO: meta tags, Open Graph, sitemap dinâmico, structured data (JSON-LD)

**Critério de saída:** 10 profissionais reais cadastrados em João Pessoa, primeiros leads registrados.

---

## Fase 2 — Avaliações e Confiança (semanas 11–16)
**Objetivo:** construir o sistema de reputação que diferencia a plataforma.

- [ ] CRUD de `Review` — cliente deixa avaliação após contato
- [ ] Cálculo e exibição de nota média no perfil
- [ ] Importação de avaliações do Google Business Profile (Google My Business API)
- [ ] Sistema de `Verification`: fluxo de solicitação + checagem manual pelo admin
- [ ] Badge "Verificado" no perfil
- [ ] Painel administrativo mínimo (aprovar verificações, moderar avaliações)
- [ ] Notificação WhatsApp ao profissional quando recebe nova avaliação

**Critério de saída:** profissionais com 3+ avaliações reais, pelo menos 5 verificados.

---

## Fase 3 — Monetização (semanas 17–24)
**Objetivo:** primeira receita recorrente.

- [ ] Modelo freemium: limite de 3 fotos no plano FREE
- [ ] Plano Premium (R$ 29,90/mês): portfólio ilimitado, destaque na busca, estatísticas
- [ ] Integração de pagamento: Stripe ou Pagar.me (cartão + PIX recorrente)
- [ ] Dashboard do profissional: visualizações de perfil, leads recebidos, posição na busca
- [ ] Destaque pago (`boost`) por período: profissional aparece no topo por 7/30 dias
- [ ] E-mail de onboarding automatizado para novos cadastros

**Critério de saída:** primeiros R$ 500/mês de receita recorrente.

---

## Fase 4 — Escala Regional (semanas 25–40)
**Objetivo:** expansão para outras cidades paraibanas e nordestinas.

- [ ] Suporte a múltiplos estados (PE, RN, CE, MA)
- [ ] Geolocalização por raio: `/api/professionals?lat=&lng=&radius=`
- [ ] App mobile via PWA com instalação no Android
- [ ] Busca por voz (integração com input de pesquisa)
- [ ] Parcerias com SENAI/SEBRAE para cadastro em massa
- [ ] Blog/conteúdo SEO: "Como contratar um eletricista em João Pessoa"
- [ ] Programa de indicação: profissional indica colega e ganha desconto no Premium

---

## Backlog (sem prazo definido)

- Integração com WhatsApp Business API para notificações em-app
- Agendamento integrado (calendário no perfil do profissional)
- Matching por IA (ranking preditivo por histórico + avaliações)
- App nativo Android/iOS (Kotlin Multiplatform — apenas após validação do modelo)
- API pública para integrações (construtoras, imobiliárias)

---

## Notas

- Itens podem ser reordenados conforme feedback dos primeiros usuários
- Cada fase deve ser revisada antes de iniciar a próxima
- Funcionalidades não planejadas que surgirem devem ser registradas no backlog antes de implementadas
