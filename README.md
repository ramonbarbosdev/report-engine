# Report Engine

Motor reutilizavel de relatorios com **Spring Boot**, **JasperReports** e **admin Angular**.

Cada deploy aponta para o banco do sistema principal via variaveis de ambiente. Nao e multi-tenant: uma instancia por servidor/cliente.

## Estrutura

```
report-engine/
├── backend/                 # API Spring Boot (geracao + admin)
├── admin-ui/                # Front administrativo Angular
├── data/templates/          # Templates JRXML versionados por deploy
├── docker-compose.yml         # PostgreSQL de metadados
└── .env.example
```

## Requisitos

- Java 21
- Maven 3.9+
- Node 20+ (admin)
- Docker (opcional, para o banco de metadados)

## Subir banco de metadados

```bash
docker compose up -d
```

## Configurar ambiente

Copie `.env.example` para `.env` na raiz do projeto:

```bash
cp .env.example .env
```

O backend carrega esse arquivo automaticamente via `java-dotenv` **antes** do Spring iniciar. Variaveis ja definidas no sistema operacional nao sao sobrescritas.

O arquivo e procurado em:
- `backend/.env` (quando roda `mvn spring-boot:run` dentro de `backend/`)
- `report-engine/.env` (um nivel acima)

Variaveis principais:

| Variavel | Descricao |
|----------|-----------|
| `REPORT_DB_METADATA_URL` | Banco de metadados do engine |
| `REPORT_DATASOURCE_PRINCIPAL_URL` | Banco read-only do sistema principal |
| `REPORT_AUTH_API_KEY` | Chave para autenticacao entre sistemas |
| `REPORT_TEMPLATES_PATH` | Pasta dos templates JRXML (padrao: `data/templates`; o backend localiza essa pasta automaticamente a partir do diretorio de execucao) |

## Rodar backend

```bash
cd backend
mvn spring-boot:run
```

API em `http://localhost:8085`.

## Rodar admin

```bash
cd admin-ui
npm install
npm start
```

Admin em `http://localhost:4300`.

## Endpoints principais

### Geracao (sistemas clientes)

```http
GET  /api/v1/reports
GET  /api/v1/reports/{cdRelatorio}/definition
POST /api/v1/reports/{cdRelatorio}/generate
Header: X-API-KEY: dev-api-key-change-me
```

Exemplo de geracao:

```bash
curl -X POST "http://localhost:8085/api/v1/reports/exemplo-visitas/generate" \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: dev-api-key-change-me" \
  -d "{\"tpFormatoSaida\":\"PDF\",\"filtros\":{\"dataInicio\":\"2026-01-01\",\"dataFim\":\"2026-01-31\"}}" \
  --output relatorio.pdf
```

### Admin

```http
GET  /admin/reports
POST /admin/reports
PUT  /admin/reports/{cdRelatorio}
PUT  /admin/reports/{cdRelatorio}/queries
PUT  /admin/reports/{cdRelatorio}/filters
POST /admin/reports/{cdRelatorio}/templates
Header: X-API-KEY: dev-api-key-change-me
```

## Integracao com app-agrotech

No `RelatorioService`, a URL pode apontar para o engine:

```typescript
const url = `${environment.reportEngineUrl}/api/v1/reports/${reportCode}/generate`;
```

Payload sugerido:

```json
{
  "tpFormatoSaida": "PDF",
  "filtros": {
    "dataInicio": "2026-01-01",
    "dataFim": "2026-01-31"
  }
}
```

## Relatorio piloto: Relacao de Visitas

Migrado do agrotech (`geral/relacaovisita` -> `relacao-visitas`).

| Item | Valor |
|------|-------|
| `cdRelatorio` | `relacao-visitas` |
| Filtros | `dataInicial`, `dataFinal`, `idProjeto`, `idTecnico` |
| Template | `data/templates/relacao-visitas/v1.jrxml` |
| Migration | `V4__agrotech_relacao_visitas.sql` |

No agrotech, o `RelatorioService` redireciona automaticamente quando `reportEngineUrl` e `reportEngineApiKey` estao no `environment.ts`.

**Importante:** valide os nomes das tabelas/colunas no banco agrotech. A SQL foi montada a partir do front e pode precisar de ajuste fino se o schema legado diferir.


A migration inicial cria o relatorio `exemplo-visitas` com:

- query simples (`SELECT 1 ...`)
- filtros `dataInicio` e `dataFim`
- template em `data/templates/exemplo-visitas/v1.jrxml`

## Proximos passos sugeridos

1. Fila assincrona para relatorios pesados
2. Endpoint de opcoes dinamicas para filtros `SELECT`
3. Versionamento com rollback de template
4. Metricas/agregacoes pos-query
5. Proxy no backend principal para nao expor API key no browser
