# Humanize Backend Scaffold

Spring Boot microservices scaffold with shared contracts and Kafka events.

## Prerequisites

- JDK 21+ (must include `javac`, not JRE-only runtime).
- No global Maven install required (`./mvnw` is included).

## Modules

- `platform/shared-contracts`: domain event envelope, event types, payload records.
- `platform/shared-kafka`: topic constants and reusable `DomainEventPublisher`.
- `services/*`: each microservice Spring Boot app.

## Services and ports

- `api-gateway` -> `8080`
- `auth-service` -> `8081`
- `library-service` -> `8082`
- `ingestion-service` -> `8083`
- `reader-service` -> `8084`
- `activity-service` -> `8085`
- `recommendation-service` -> `8086`
- `notification-service` -> `8087`
- `ai-service` -> `8088`

## Kafka topics

- `user.lifecycle`
- `book.uploaded`
- `book.processing`
- `reader.progress`
- `reader.activity`
- `notification.lifecycle`

`book.processing` now carries:
- `BOOK_PROCESSING_STARTED`
- `BOOK_METADATA_EXTRACTED`
- `BOOK_PROCESSING_COMPLETED`
- `BOOK_PROCESSING_FAILED`

## Run example

```bash
cd backend
./mvnw -pl services/library-service -am spring-boot:run
```

## MVP Auth + Upload endpoints

Auth service:

- `POST /api/auth/google/id-token` (verify Google ID token, issue app JWTs)
- `POST /api/auth/refresh` (rotate access/refresh token pair)

Library service:

- `POST /api/library/books/upload-intent` (generate R2 pre-signed upload URL)
- `POST /api/library/books/uploaded` (emit `BOOK_UPLOADED` Kafka event)
- `GET /api/library/books/user/{userId}` (DB-backed user library state)

Ingestion service:

- Consumes `BOOK_UPLOADED`, resolves source from local mirror or direct R2 download, then extracts text and emits processing lifecycle events.
- `GET /api/ingestion/books/{bookId}/status` (view current extraction state)

Reader service:

- `POST /api/reader/progress` (persist progress + emit `READER_PROGRESS_UPDATED`)
- `GET /api/reader/progress/{userId}/{bookId}` (latest progress for one user/book)
- `GET /api/reader/progress/user/{userId}` (latest progress list for user)

Activity service:

- Consumes `READER_PROGRESS_UPDATED`, persists activity events, emits `READER_ACTIVITY_RECORDED`.
- `GET /api/activity/users/{userId}/events`
- `GET /api/activity/books/{bookId}/events`

Recommendation service:

- Consumes `READER_PROGRESS_UPDATED`, `READER_ACTIVITY_RECORDED`, and `BOOK_METADATA_EXTRACTED`.
- Stores book features + user signals + recommendation scores in DB.
- `GET /api/recommendations/{userId}` returns ranked, scored recommendations.

## Hardened-System Setup (No sudo)

If your system blocks package installation, use a user-local JDK:

1. Download/extract a JDK 21 in `~/jdks/jdk-21` (or any folder you own).
2. Run Maven through the helper script:

```bash
cd backend
./scripts/with-local-jdk.sh -DskipTests clean compile
./scripts/with-local-jdk.sh -pl services/library-service -am spring-boot:run
```

You can also override JDK location per command:

```bash
cd backend
HUMANIZE_JDK_HOME="$HOME/jdks/jdk-21.0.6" ./scripts/with-local-jdk.sh -v
```

## Local Infra (Postgres + Kafka)

```bash
cd backend
docker compose -f infra/docker-compose.local.yml up -d
```

Local endpoints:
- Postgres: `localhost:5432` (`humanize` / `humanize`)
- Kafka bootstrap: `localhost:19092`
- Kafka console UI: `http://localhost:8089`

Service env template:
- Copy `infra/local-dev.env.example` to `infra/local-dev.env`
- Source it before running services from terminal:

```bash
set -a
source infra/local-dev.env
set +a
```

## One-Command Smoke Flow

1. Copy local env file once:

```bash
cd backend
cp infra/local-dev.env.example infra/local-dev.env
```

2. Start infra + default services + readiness checks:

```bash
./scripts/smoke.sh up
```

3. Useful smoke commands:

```bash
# Start only selected services
./scripts/smoke.sh up auth-service library-service ingestion-service

# View overall status (infra + services)
./scripts/smoke.sh status

# Run ping checks against running services
./scripts/smoke.sh check

# Follow logs
./scripts/smoke.sh logs infra
./scripts/smoke.sh logs library-service

# Stop everything
./scripts/smoke.sh down
```

Lower-level helpers:
- `./scripts/local-infra.sh` for Docker infra only.
- `./scripts/local-services.sh` for service process lifecycle only.

## Golden-Path E2E Smoke

Run a full event chain test (library -> ingestion -> reader -> activity -> recommendation):

```bash
cd backend
./scripts/e2e-smoke.sh
```

What it does:
- Starts infra + core services (unless `--no-up` is used).
- Creates an isolated e2e env file with local ingestion mirror overrides.
- Tries `/upload-intent` and falls back automatically if R2 creds are not configured.
- Generates a valid local PDF for the emitted object key.
- Emits `/uploaded`, waits for `COMPLETED` ingestion, posts reader progress.
- Verifies activity and recommendation results for the same `userId/bookId`.

Optional flags:
- `--no-up` to skip start and only run checks against already running services.
- `--user-id <id>` and `--book-id <id>` to control test IDs.
- `--env-file <path>` to use a non-default env file as base.
- `--timeout-seconds <n>` to adjust poll timeout.

## Notes

- Set `KAFKA_BOOTSTRAP_SERVERS` to your Aiven Kafka bootstrap endpoint.
- Auth env vars:
`GOOGLE_CLIENT_ID`, `AUTH_JWT_SECRET`, `AUTH_ISSUER` (optional).
- Auth DB env vars:
`AUTH_DB_URL`, `AUTH_DB_USERNAME`, `AUTH_DB_PASSWORD`, `AUTH_DB_DRIVER`, `AUTH_DB_DDL_AUTO`, `AUTH_FLYWAY_ENABLED`, `AUTH_FLYWAY_BASELINE_ON_MIGRATE`.
- R2 env vars:
`R2_ACCESS_KEY_ID`, `R2_SECRET_ACCESS_KEY`, `R2_BUCKET`, and either `R2_ENDPOINT` or `R2_ACCOUNT_ID`.
- Library DB env vars:
`LIBRARY_DB_URL`, `LIBRARY_DB_USERNAME`, `LIBRARY_DB_PASSWORD`, `LIBRARY_DB_DRIVER`, `LIBRARY_DB_DDL_AUTO`, `LIBRARY_FLYWAY_ENABLED`, `LIBRARY_FLYWAY_BASELINE_ON_MIGRATE`.
- Ingestion env vars:
`INGESTION_LOCAL_SOURCE_ROOT`, `INGESTION_OCR_ENABLED`, `INGESTION_OCR_COMMAND`, `INGESTION_MIN_EXTRACTED_CHARS`.
- Ingestion uses R2 vars too (`R2_ACCESS_KEY_ID`, `R2_SECRET_ACCESS_KEY`, `R2_BUCKET`, `R2_ENDPOINT`/`R2_ACCOUNT_ID`) when local mirror file is unavailable.
- Ingestion DB env vars:
`INGESTION_DB_URL`, `INGESTION_DB_USERNAME`, `INGESTION_DB_PASSWORD`, `INGESTION_DB_DRIVER`, `INGESTION_DB_DDL_AUTO`, `INGESTION_FLYWAY_ENABLED`, `INGESTION_FLYWAY_BASELINE_ON_MIGRATE`.
- Reader DB env vars:
`READER_DB_URL`, `READER_DB_USERNAME`, `READER_DB_PASSWORD`, `READER_DB_DRIVER`, `READER_DB_DDL_AUTO`, `READER_FLYWAY_ENABLED`, `READER_FLYWAY_BASELINE_ON_MIGRATE`.
- Activity DB env vars:
`ACTIVITY_DB_URL`, `ACTIVITY_DB_USERNAME`, `ACTIVITY_DB_PASSWORD`, `ACTIVITY_DB_DRIVER`, `ACTIVITY_DB_DDL_AUTO`, `ACTIVITY_FLYWAY_ENABLED`, `ACTIVITY_FLYWAY_BASELINE_ON_MIGRATE`.
- Recommendation DB env vars:
`RECO_DB_URL`, `RECO_DB_USERNAME`, `RECO_DB_PASSWORD`, `RECO_DB_DRIVER`, `RECO_DB_DDL_AUTO`, `RECO_FLYWAY_ENABLED`, `RECO_FLYWAY_BASELINE_ON_MIGRATE`.
- DB migration scripts live at `src/main/resources/db/migration` per service; JPA now defaults to `validate` mode.
- This scaffold focuses on service boundaries + event wiring, not production logic.
