# AI Gateway

![logo](docs/images/logo.png)

A unified API gateway for multiple AI providers (Gemini, OpenAI, Anthropic) with per-project cost controls, usage tracking, and capability-based routing.

## What It Does

AI Gateway sits between your application and AI providers. Instead of integrating directly with OpenAI, Anthropic, or Google, your app calls one endpoint. The gateway handles provider selection, authentication, cost tracking, and rate limiting.

```
Your App → AI Gateway → [Gemini | OpenAI | Anthropic]
                ↓
           PostgreSQL (projects, keys, usage, credentials)
```

## Key Features

- **Unified API**: OpenAI-compatible endpoint works with any provider
- **Capability Routing**: Request `fast`, `balanced`, `best`, or `thinking` - gateway picks the right model
- **Per-Project Isolation**: Each project has its own API keys, provider credentials, and limits
- **Cost Controls**: Set daily/monthly token and dollar limits per project
- **Usage Tracking**: Every request is logged with tokens, cost, latency, and status
- **Encrypted Credentials**: Provider API keys stored with AES-256-GCM encryption
- **Chat UI**: Built-in web interface for testing and ad-hoc usage

## Tech Stack

- **Backend**: Kotlin, Spring Boot 3, WebFlux (reactive)
- **Database**: PostgreSQL with R2DBC
- **Auth**: JWT with HttpOnly cookies
- **Frontend**: Thymeleaf, Tailwind CSS, HTMX

## Prerequisites

- Java 21+
- PostgreSQL 14+
- Gradle 8+ (or use the included wrapper)

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/phanisaripalli/ai-gateway.git
cd ai-gateway
```

### 2. Create the database

```bash
# For local PostgreSQL
psql -U postgres -c "CREATE DATABASE ai_gateway;"
psql -U postgres -d ai_gateway -f schema.sql

# For remote/cloud PostgreSQL
psql "postgresql://user:password@your-host:5432/ai_gateway" -f schema.sql
```

### 3. Configure environment

```bash
cp .env.example .env
```

Edit `.env` with your values:

```bash
# --- Database (PostgreSQL) ---
DB_HOST=localhost              # or your-db-host.example.com
DB_PORT=5432
DB_NAME=ai_gateway             # your database name
DB_USER=postgres               # your database user
DB_PASSWORD=your_db_password

# --- Admin Account (created on first startup) ---
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=your_secure_password

# --- Security ---
# Generate with: openssl rand -base64 32
JWT_SECRET=your_jwt_secret_here

# Generate with: openssl rand -base64 32
ENCRYPTION_KEY=your_encryption_key_here
```

### 4. Run the server

```bash
# Load environment and run
source .env && ./gradlew bootRun
```

The server starts at `http://localhost:8080`

### 5. Access the dashboard

1. Open `http://localhost:8080/login`
2. Login with your admin credentials
3. Create a project
4. Add provider API keys (Gemini/OpenAI/Anthropic)
5. Generate a gateway API key

## Usage

### API Endpoint

```bash
POST /v1/chat/completions
Authorization: Bearer gw_your_api_key
Content-Type: application/json
```

### Basic Request

```bash
curl http://localhost:8080/v1/chat/completions \
  -H "Authorization: Bearer gw_your_key" \
  -H "Content-Type: application/json" \
  -d '{
    "capability": "fast",
    "messages": [{"role": "user", "content": "Hello!"}]
  }'
```

### Request with Provider

```bash
curl http://localhost:8080/v1/chat/completions \
  -H "Authorization: Bearer gw_your_key" \
  -H "Content-Type: application/json" \
  -d '{
    "capability": "balanced",
    "provider": "openai",
    "messages": [{"role": "user", "content": "Explain quantum computing"}]
  }'
```

### Direct Model Request

```bash
curl http://localhost:8080/v1/chat/completions \
  -H "Authorization: Bearer gw_your_key" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "claude-sonnet-4-5",
    "messages": [{"role": "user", "content": "Write a haiku"}]
  }'
```

### Chat UI

Access the built-in chat interface at `http://localhost:8080/chat`

## Capability Routing

The API accepts both `capability` and `provider`. Routing priority:
1. If `provider` is specified → use that provider
2. Else if project has `default_provider` → use that
3. Else → system default (gemini → openai → anthropic)

### Model Mapping

| Capability | Gemini | OpenAI | Anthropic |
|------------|--------|--------|-----------|
| `fast` | gemini-2.0-flash | gpt-4.1-mini | claude-haiku-4-5 |
| `balanced` | gemini-2.5-pro | gpt-4.1 | claude-sonnet-4-5 |
| `thinking` | gemini-2.5-flash-thinking | o3 | claude-sonnet-4-5 |
| `best` | gemini-2.5-pro | gpt-4.1 | claude-opus-4-5 |

## API Reference

| Endpoint | Description |
|----------|-------------|
| `POST /v1/chat/completions` | Chat completion (OpenAI-compatible) |
| `GET /api/v1/projects` | List projects |
| `POST /api/v1/projects` | Create project |
| `POST /api/v1/projects/{id}/keys` | Generate API key |
| `POST /api/v1/projects/{id}/credentials` | Add provider key |
| `GET /api/v1/stats/overview` | Dashboard statistics |

## Database Schema

```sql
-- Admin users for dashboard access
admins (id, email, password_hash, created_at, last_login_at)

-- Projects with usage limits
projects (id, name, description, daily_token_limit, daily_cost_limit,
          monthly_token_limit, monthly_cost_limit, default_provider,
          default_capability, is_active, created_at, updated_at)

-- Gateway API keys (for clients calling the gateway)
api_keys (id, project_id, key_prefix, key_hash, name,
          rate_limit_rpm, is_active, created_at, last_used_at)

-- Provider credentials (encrypted API keys for upstream providers)
provider_credentials (id, project_id, provider, encrypted_key, created_at)

-- Request log
requests (id, project_id, api_key_id, provider, model, capability,
          input_tokens, output_tokens, thinking_tokens, cost_usd,
          latency_ms, status, error_code, error_message, created_at)

-- Daily usage aggregates per project
usage_counters (project_id, date, total_tokens, total_cost_usd, request_count)

-- Daily usage aggregates per provider
provider_usage (provider, date, total_tokens, total_cost, request_count)
```

## Multi-Tenant Use Case

1. Create a project per customer
2. Customer adds their own provider API key
3. Customer's usage is isolated and tracked separately
4. Bill customers based on their usage data

## Screenshots
![AdminSetup](docs/images/dashboard_1.png)
![AdminCosts](docs/images/dashboard_2.png)
![Chat1](docs/images/chat_1.png)
![Chat2](docs/images/chat_2.png)


## Docker

_Coming soon_

## License

MIT
