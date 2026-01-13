# Mini TNG (E-Wallet) Project Plan

## Project Overview
**Project Name:** Mini TNG (Touch 'n Go–style e-Wallet)

**Description:**
A microservices-based digital wallet system that simulates wallet balance, top-ups, transfers, AI spending analysis, and Stripe payments. Features secure transaction handling, event-driven architecture, real-time balance management, and observability.

**Tech Stack:**
- Spring Boot
- PostgreSQL
- Redis
- Kafka
- Stripe Payment
- Spring Cloud Gateway
- Prometheus + Grafana
- Spring @Async / Virtual Threads
- Testcontainers
- Docker
- Flyway
- Spring Security + JWT
- Optional: OpenSearch for analytics

---

## Architecture
```
[ Mobile / Web Frontend ]
        |
   [ API Gateway ]
        |
---------------------------------------------------------
| Auth | Wallet | Transaction | Payment | AI | Notify |
---------------------------------------------------------
        |        |            |           |
     Postgres   Redis       Kafka       Stripe
```

## Services Overview

### 1. Auth / User Service
- Responsibilities:
  - User registration/login
  - Role & plan management (FREE / PREMIUM)
- Tech:
  - JWT authentication
  - Redis: session cache, token blacklist
- Security:
  - Spring Security + OAuth2

### 2. Wallet Service
- Responsibilities:
  - Wallet creation & balance queries
  - Atomic balance updates
  - Freeze / lock funds for transactions
- Tech:
  - Postgres: wallets table
  - Redis: balance cache, distributed locks
  - Flyway: DB migrations

### 3. Transaction Service
- Responsibilities:
  - Wallet-to-wallet transfers
  - Payment to merchant
  - Transaction history logging
  - Idempotency handling
- Tech:
  - Kafka: `transaction-created`, `transaction-success`, `transaction-failed`
  - Redis: idempotency keys
  - @Async / Virtual Threads for high throughput

### 4. Payment Service (Stripe)
- Responsibilities:
  - Top-ups
  - Stripe checkout session handling
  - Webhook processing
  - Emit Kafka events (`PAYMENT_SUCCESS`, `PAYMENT_FAILED`)
- Tech:
  - Redis: idempotency for webhooks, rate limiting
  - Stripe SDK integration

### 5. Notification Service
- Responsibilities:
  - Send email notifications (transaction success/failure, balance updates)
  - Consume Kafka events
- Tech:
  - Spring Mail
  - Async processing

### 6. AI Service (Optional/Advanced)
- Responsibilities:
  - Analyze spending patterns
  - Fraud pattern detection hints
  - Generate monthly summaries
- Tech:
  - Optional LLM integration
  - Redis cache for AI responses

### 7. Observability
- Prometheus: metrics collection
- Grafana: dashboards for latency, transactions, errors
- Optional OpenTelemetry + Jaeger for distributed tracing

### 8. Testing
- Testcontainers for Postgres & Kafka integration tests
- JUnit 5 & Mockito for unit & integration tests

### 9. Infrastructure
- Docker / Docker Compose for service orchestration
- Flyway for DB migrations
- Optional Kubernetes for deployment

---

## Key Features / Flow Examples

### Wallet Top-Up
1. User initiates top-up from frontend
2. Payment Service creates Stripe checkout session
3. Stripe webhook confirms payment
4. Payment Service publishes Kafka event
5. Wallet Service credits balance
6. Notification Service sends confirmation email
7. Redis ensures idempotency and prevents double-credit

### Wallet Transfer
1. User initiates transfer
2. Wallet Service locks sender balance (Redis distributed lock)
3. Transaction Service creates transaction record
4. Wallet balances updated atomically
5. Kafka event emitted
6. Notification sent to sender & recipient

### AI Spending Analysis
1. AI Service consumes transaction data or triggered by request
2. Generates insights (spending pattern, fraud alert)
3. Responses cached in Redis
4. Insights returned to user dashboard

---

## Redis Usage
- Balance caching
- Distributed locks (prevent double-spending)
- Idempotency keys for payments & webhooks
- Session & token caching
- Rate limiting
- Caching AI response/summary

## Kafka Usage
- Event-driven communication between services
- Transaction events
- Payment events
- Notification triggers

## Stripe Integration
- Checkout sessions
- Payment & subscription handling
- Webhook processing with idempotency

## Optional Advanced Features
- OpenSearch / Elasticsearch for analytics & transaction search
- Distributed tracing (OpenTelemetry + Jaeger)
- Saga pattern for multi-service transaction consistency

---

## Development Milestones (Suggested)
1. Week 1-2: Wallet & User Service + Postgres + Redis + Flyway + Security
2. Week 3: Transaction Service + Kafka + Idempotency + Async processing
3. Week 4: Payment Service + Stripe integration + webhook + Kafka events
4. Week 5: Notification Service + Mail + Kafka consumption
5. Week 6: AI Service integration + Redis caching + optional analytics
6. Week 7: Observability (Prometheus + Grafana) + Testing (Testcontainers)
7. Week 8: Dockerize services + optional Kubernetes setup + end-to-end integration tests

---

## Resume / Interview Highlights
- Microservices-based wallet system with event-driven architecture
- ACID + eventual consistency for transactions
- Stripe payment integration with idempotent webhook handling
- Redis for caching, locks, and rate-limiting
- Kafka for reliable async communication
- Observability stack (Prometheus, Grafana, optional tracing)
- AI-powered spending analysis (optional)
- Secure authentication & JWT management
- Full testing with Testcontainers and JUnit
- Dockerized for reproducible development environment

