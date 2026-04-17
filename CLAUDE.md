# CLAUDE.md — Trabalho Arretado (raiz do monorepo)

Este é o repositório principal do **Trabalho Arretado**,
uma plataforma de divulgação e conexão de profissionais autônomos no Nordeste brasileiro.

## Estrutura do monorepo

```
trabalho-arretado/
├── api/          ← Backend Kotlin/Ktor       →  leia api/CLAUDE.md
├── android/      ← App Android (Kotlin/Compose) →  leia android/CLAUDE.md
└── docs/         ← Documentação viva do projeto
```

> Antes de atuar em qualquer subprojeto, leia o CLAUDE.md específico da pasta.
> Para contexto de negócio, domínio e decisões técnicas, consulte os arquivos em `docs/`.

## Documentação essencial

| Arquivo | Conteúdo |
|---|---|
| `docs/CONTEXT.md` | Visão geral do produto, público-alvo e proposta de valor |
| `docs/DOMAIN.md` | Entidades, agregados e linguagem ubíqua do domínio |
| `docs/ROADMAP.md` | Fases de desenvolvimento e features planejadas |
| `docs/DECISIONS.md` | ADRs — decisões técnicas e suas justificativas |
| `docs/DATA_GUIDE.md` | Convenções de dados, nomenclatura e modelagem |

## Convenções globais

- Idioma do código: **inglês** (variáveis, funções, classes, rotas, campos de banco)
- Idioma da UI e comunicação com usuário: **português brasileiro**
- Idioma desta documentação: **português brasileiro**
- Commits seguem **Conventional Commits**: `feat:`, `fix:`, `docs:`, `chore:`, `refactor:`
- Nunca commitar secrets, `.env` ou credenciais — usar `.env.example` como referência

## Como rodar o projeto completo

```bash
# Backend (porta 8080)
cd api && ./gradlew run

# App Android — abrir a pasta android/ no Android Studio e executar via IDE
# ou via linha de comando (emulador já iniciado):
cd android && ./gradlew installDebug
```
