# Trabalho Arretado

> Plataforma de divulgação e conexão de profissionais autônomos no Nordeste brasileiro.

---

## O que é

O Trabalho Arretado conecta quem precisa de um serviço — reforma, elétrica, hidráulica, marcenaria, pintura e mais — com profissionais autônomos verificados na Paraíba e no Nordeste. A plataforma digitaliza o boca-a-boca regional, oferecendo perfis com portfólio visual, avaliações e contato direto via WhatsApp.

## Estrutura do repositório

```
trabalho-arretado/
├── api/          ← Backend: Kotlin + Ktor
├── web/          ← Frontend: Next.js + Tailwind CSS + shadcn/ui
└── docs/         ← Documentação de produto, domínio e decisões técnicas
```

## Requisitos

- **Backend:** JDK 17+, Gradle 8+
- **Frontend:** Node.js 20+, npm 10+

## Rodando localmente

```bash
# Clonar o repositório
git clone <repo-url>
cd trabalho-arretado

# Backend
cd api
cp .env.example .env   # preencher variáveis
./gradlew run

# Frontend (outro terminal)
cd web
cp .env.example .env.local   # preencher variáveis
npm install
npm run dev
```

A API estará disponível em `http://localhost:8080` e o frontend em `http://localhost:3000`.

## Documentação

Consulte a pasta `docs/` para contexto de produto, modelagem de domínio, roadmap e decisões técnicas.

## Licença

Privado — todos os direitos reservados.
