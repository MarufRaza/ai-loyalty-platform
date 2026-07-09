# AI-Powered Customer Loyalty & Marketing Automation Platform

> A production-grade SaaS platform for customer loyalty management, AI-driven campaign automation, and real-time analytics — built with Java 21, Spring Boot 3.3, React 18, and cloud AI via Groq.

[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.3-61dafb)](https://react.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ed)](https://docs.docker.com/compose/)

---

## Features

| Domain | Capabilities |
|--------|-------------|
| **Auth** | JWT + Refresh Token rotation, RBAC (Admin / Marketing Manager / Customer Support) |
| **Customer Management** | Full CRUD, search & filter, soft delete, lifetime value tracking |
| **Loyalty Engine** | Points EARN/REDEEM/EXPIRE, automatic tier upgrades (Silver → Gold → Platinum), tier discounts |
| **Coupons** | Auto-generated codes, flat/percentage/free-shipping types, tier requirements, usage limits |
| **Segmentation** | Rule-based customer segments with filter criteria |
| **AI Campaigns** | Cloud AI generation via **Groq + Llama 3.3 70B** — free (14,400 req/day), ultra-fast, no hardware needed |
| **Campaign Lifecycle** | Draft → Scheduled → Running → Completed, multi-channel (Email/WhatsApp/SMS/Push) |
| **Analytics** | Real-time dashboard with monthly growth, tier distribution, campaign performance |
| **Audit Logging** | Async non-blocking audit trail for every user action with IP tracking |
| **Monitoring** | Prometheus metrics + Grafana dashboards, custom business metrics |
| **Security** | Rate limiting (Bucket4j), CORS, CSRF protection, bcrypt-12 password hashing |
| **API Docs** | Swagger UI (SpringDoc 2.5), OpenAPI 3.1 |

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Docker Network                        │
│                                                             │
│  ┌──────────┐   ┌──────────────┐   ┌────────────────────┐  │
│  │  React   │   │  Spring Boot │   │    PostgreSQL 16    │  │
│  │  Vite    │──▶│  Java 21     │──▶│    HikariCP pool   │  │
│  │  Nginx   │   │  Port 8080   │   │    Port 5432       │  │
│  │  Port 80 │   └──────────────┘   └────────────────────┘  │
│  └──────────┘          │                                    │
│                        ▼                                    │
│              ┌─────────────────┐   ┌────────────────────┐  │
│              │  Groq + Llama 3.3  │   │ Prometheus/Grafana  │  │
│              │  (Free AI API)     │   │ Port 9090 / 3000   │  │
│              └─────────────────┘   └────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

**Backend Package Structure:**
```
com.loyaltyplatform/
├── config/          # Security, Swagger, Metrics, AI config
├── controller/      # REST controllers (9)
├── dto/             # Request & Response DTOs
├── entity/          # JPA entities (10)
├── enums/           # Domain enums (7)
├── exception/       # Global exception handler
├── repository/      # Spring Data JPA repos (9)
└── service/         # Business logic services (10)
```

---

## Quick Start

### Prerequisites
- Docker 24+ & Docker Compose 2+
- (Optional) Java 21 & Node 20 for local development

### 1. Clone & Configure
```bash
git clone https://github.com/YOUR_USERNAME/ai-loyalty-platform.git
cd ai-loyalty-platform
cp frontend/.env.example frontend/.env.local
```

### 2. Get Free Groq API Key
Go to [console.groq.com](https://console.groq.com) → API Keys → Create → copy the key.

### 3. Start All Services
```bash
docker compose up --build -d
```

| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| Grafana | http://localhost:3001 (admin/admin) |
| Prometheus | http://localhost:9090 |

### 4. Create Admin User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Admin","email":"admin@example.com","password":"Admin@1234","role":"ROLE_ADMIN"}'
```

---

## Local Development (without Docker)

### Backend
```bash
cd backend
# Requires PostgreSQL running on localhost:5432
# Set env vars or edit application.yml
./mvnw spring-boot:run -Dspring-boot.run.profiles=default
```

### Frontend
```bash
cd frontend
npm install
npm run dev   # http://localhost:5173
```

---

## API Reference

### Authentication
```
POST /api/auth/register   – Register new user
POST /api/auth/login      – Login (returns access + refresh token)
POST /api/auth/refresh    – Rotate refresh token
POST /api/auth/logout     – Revoke all refresh tokens
```

### Customers
```
GET    /api/customers          – List/search customers (paginated)
POST   /api/customers          – Create customer
GET    /api/customers/:id      – Get customer details
PUT    /api/customers/:id      – Update customer
DELETE /api/customers/:id      – Soft delete customer
```

### Loyalty
```
POST /api/loyalty/transactions                  – Process transaction (EARN/REDEEM/BONUS)
GET  /api/loyalty/customers/:id/transactions   – Transaction history
```

### Campaigns
```
GET    /api/campaigns          – List campaigns (status filter)
POST   /api/campaigns          – Create campaign
GET    /api/campaigns/:id      – Get campaign
PUT    /api/campaigns/:id      – Update campaign
POST   /api/campaigns/:id/publish  – Publish campaign
POST   /api/campaigns/:id/cancel   – Cancel campaign
```

### AI Campaign Generation
```
POST /api/ai/campaigns/generate  – Generate campaign content with Groq
```

Request body:
```json
{
  "campaignType": "EMAIL",
  "objective": "Re-engage inactive Gold customers",
  "tone": "Friendly",
  "brandName": "MyBrand",
  "segmentId": 1
}
```

### Coupons
```
GET    /api/coupons            – All coupons
GET    /api/coupons/valid      – Active & non-expired coupons
GET    /api/coupons/:code      – Get by code
POST   /api/coupons            – Create coupon
POST   /api/coupons/:code/redeem – Redeem coupon
DELETE /api/coupons/:id        – Deactivate coupon
```

### Analytics
```
GET /api/analytics/dashboard   – Full platform analytics snapshot
```

### Audit Logs (Admin only)
```
GET /api/audit-logs   – Paginated logs with filters (action, entityType, dateFrom, dateTo)
```

---

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://postgres:5432/loyalty_db` |
| `DATABASE_URL` | PostgreSQL URL from Neon/Render (plain `postgresql://...`) | `postgresql://user:pass@host:port/dbname?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | DB username | `loyalty_user` |
| `SPRING_DATASOURCE_PASSWORD` | DB password | *(set in compose)* |
| `JWT_SECRET` | 256-bit base64 JWT secret | *(generate with `openssl rand -base64 32`)* |
| `AI_API_KEY` | Groq API key (get free at console.groq.com) | *(required for AI features)* |
| `AI_MODEL` | AI model name | `llama-3.3-70b-versatile` |
| `AI_BASE_URL` | AI provider base URL | `https://api.groq.com/openai/v1` |
| `VITE_API_BASE_URL` | Frontend API base | `/api` |

---

## Running Tests
```bash
cd backend
./mvnw test -Ptest
```

Tests use H2 in-memory database (profile: `test`). Covers:
- `AuthServiceTest` — register, duplicate email, login, bad credentials
- `CustomerServiceTest` — CRUD, soft delete, tier progress
- `LoyaltyServiceTest` — earn, redeem, insufficient points, tier upgrade
- `AuthControllerTest` — integration tests with MockMvc

---

## Deployment

### Cloud (Render + Neon + Vercel)
1. **Database**: Create a free PostgreSQL on [Neon](https://neon.tech) — copy the connection string
2. **Backend**: Deploy to [Render](https://render.com) as a Docker service, set environment variables
3. **Frontend**: Deploy to [Vercel](https://vercel.com) — set `VITE_API_BASE_URL` to backend URL
4. **Groq**: Use the free Groq API key with `AI_API_KEY` or adapt `AICampaignService.java` for another provider.

### CI/CD
GitHub Actions workflow at `.github/workflows/ci-cd.yml`:
- **CI**: Build and test on every push/PR to `main`
- **CD**: Build and push Docker images on tag push (`v*.*.*`)

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.3.0 |
| Security | Spring Security 6, JWT (JJWT 0.12.5) |
| Database | PostgreSQL 16, Spring Data JPA, HikariCP |
| AI | Groq API (free), Llama 3.3 70B, Spring WebFlux WebClient |
| Rate Limiting | Bucket4j 8.10.1 |
| Observability | Micrometer + Prometheus + Grafana |
| API Docs | SpringDoc OpenAPI 3.1 (Swagger UI) |
| Frontend | React 18, Vite 5, Tailwind CSS 3 |
| Charts | Recharts 2 |
| HTTP Client | Axios |
| Containers | Docker, Docker Compose |
| CI/CD | GitHub Actions |

---

## License

MIT License — feel free to use for learning, portfolio projects, or production applications.
