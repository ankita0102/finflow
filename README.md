# FinFlow

FinFlow is a production-oriented financial operations platform for learning and demonstrating
backend architecture, distributed-systems patterns, cloud infrastructure, observability and
full-stack product engineering.

## Milestone 1

Implemented now:

- Spring Boot + Java 21
- PostgreSQL
- Liquibase schema migrations
- Account creation and lookup
- Payment creation and lookup
- Mandatory `Idempotency-Key`
- Duplicate-payment protection
- Ledger entries for debits/credits
- Account transaction history
- Validation and API error handling
- Swagger/OpenAPI
- Actuator health/metrics
- Docker Compose
- Unit test for idempotency

## Run

Requirements: Java 21, Maven 3.9+, Docker.

```bash
docker compose up -d
mvn spring-boot:run
```

Swagger:
`http://localhost:8080/swagger-ui.html`

Health:
`http://localhost:8080/actuator/health`

Tests:

```bash
mvn test
```

## Core API

- `POST /api/v1/accounts`
- `GET /api/v1/accounts/{id}`
- `POST /api/v1/payments`
- `GET /api/v1/payments/{id}`
- `GET /api/v1/accounts/{id}/transactions`

## Important design choice

Milestone 1 settles payments synchronously so the domain rules are clear before distributed
complexity is introduced.

Milestone 2 will introduce:

- Kafka
- `payment.created`, `payment.authorized`, `payment.failed`, `payment.settled`
- Transactional outbox
- Retry/backoff
- Dead-letter queue
- Event consumers

Later milestones:

- Next.js + TypeScript dashboard
- FastAPI analytics/fraud service
- Redis
- OpenTelemetry + Prometheus + Grafana
- Load testing
- GitHub Actions
- AWS ECS/RDS/S3/IAM
- Terraform
- optional Kubernetes + Helm
