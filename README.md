# user-silo

System of record for users in the siloverse platform. Spring Boot 4 on Kotlin, Java 21.
Listens on port `10101` and is reachable at `https://user-silo.macgrant-platform.test`
through the macgrant gateway.

## What it does

- `POST /api/users`: callers holding `ROLE_SYSTEM` only, in practice auth-silo's service
  account. Persists the user into schema `user_silo` of the shared `siloverse` Postgres
  database and publishes `user-silo.user-registered` through the messaging outbox to
  RabbitMQ vhost `siloverse`. Duplicate `keycloakId` answers 409.
- `GET /api/users/me`: echoes the caller's identity and authorities.
- Resource server against Keycloak realm `kyc`; Keycloak's `realm_access.roles` become
  `ROLE_*` authorities through a hand-written converter.

## Modules

| Module | Contents |
| --- | --- |
| `silo` | the runnable service: security, persistence, controller, outbox wiring |
| `web` | API contract DTOs, published for auth-silo |
| `messages` | event contract (`UserRegistered` + wire names), published for notification-silo |
| `ui` | placeholder |

## Persistence and messaging

Flyway migrations live in `silo/src/main/resources/db/migration` (`V1` users table,
`V2` messaging outbox) and name the schema explicitly; Hibernate only validates.
The environment grants the space (macgrant creates database, role and schema), the
application fills it.

## Observability

Traces and logs go to the macgrant OpenTelemetry collector over OTLP; metrics are
scraped by the platform's Prometheus from `/actuator/prometheus`.

## Running

1. Bring the platform up (`vagrant up` in macgrant).
2. `./gradlew :silo:bootRun`.
3. Tests: `./gradlew build` (Testcontainers, Docker required).

## Dependencies

- `io.github.siloverse:messaging-*` from java-library. Currently pinned to
  `1.0.11-SNAPSHOT` from `mavenLocal()` until messaging 1.0.11 is released.
- Build conventions from siloverse-build 1.11.1.

`CHECKPOINT.md` is the engineering journal and the arbiter of what is decided and built.
