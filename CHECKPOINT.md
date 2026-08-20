# CHECKPOINT — user-silo

_Last updated: 2026-08-20. This document is the arbiter: if Claude asserts something about this repo that isn't here or visible in the code, call it — that's drift._

**Role in the platform:** system of record for users. Resource server; will own db `user_silo` (JPA/Flyway, Phase 4) and publish `UserRegistered` via the outbox (Phase 5). Base package `io.github.siloverse.user`; modules: `silo` (assembly + business logic), `web` (API contract), `messages` (event contract, messaging-core-only), `ui`.

## Locked decisions

- **Security wiring lives in `silo` module**, package `…user.security` — app assembly, not contract. NOT extracted to java-library yet: rule of three (auth-silo and notification-silo are copies #2/#3; extraction is Phase 8.6).
- **`SecurityConfiguration`:** stateless (no HttpSession — a resource server holding session state re-invents the login world), CSRF off (defensible only because stateless: CSRF rides auto-attached cookies; bearer headers are never auto-attached), `/actuator/health/**` permitAll (infrastructure can't fetch tokens), **ERROR dispatcher permitAll** (see lesson below), anyRequest authenticated.
- **`KeycloakJwtAuthenticationConverter`** (hand-written `Converter<Jwt, AbstractAuthenticationToken>`): maps Keycloak's vendor-specific `realm_access.roles` → `ROLE_*` authorities; name = `preferred_username ?: sub` (identity fields fall back to another identity, never to ""). Replacing the default converter drops `SCOPE_*`/`FACTOR_*` authorities — unused, accepted.
- Run via `./gradlew :silo:bootRun` when IntelliJ's Run drifts — the build is truth, the IDE is a view.

## Built & green (Phase 2, tasks 2.2–2.5, verified 2026-08-20)

- Resource server active: `oauth2-resource-server` via local catalog bundle `spring-security` (versionless, platform BOM governs), `issuer-uri` → realm `kyc`. Startup fingerprint of success: NO "generated security password", `BearerTokenAuthenticationFilter` in the printed filter list.
- `GET /api/users/me` returns sub/username/email/authorities from `@AuthenticationPrincipal Jwt`.
- Verified matrix: user token → `ROLE_customer` (+profile claims); SA token → `ROLE_system` only, null profile fields; no token → 401 (bare `WWW-Authenticate: Bearer` = denied as anonymous at AuthorizationFilter); garbage token → 401 with `error="invalid_token"` (caught at BearerTokenAuthenticationFilter). Same status, different filter — the header tells you which.

## Lessons paid for (2026-08-20)

- **Unknown config keys fail silently** (`issuer-url`, `org.springframework.scurity`): relaxed binding tolerates strangers by design. Detection is the startup fingerprint, not an error message.
- **Three-layer failure chain, cost ~1h:** SA token lacks `preferred_username` (robot, no profile scope) → Kotlin platform type (`String!`) let `null` into a Java ctor → NPE at a distance → Tomcat ERROR dispatch to `/error` → own `anyRequest().authenticated()` denied the error page as anonymous → 500 laundered into a bare 401 that looked like an auth failure. Fixes: identity fallback in converter + ERROR dispatch permitAll (a client can't address the ERROR dispatch from outside; permitting it un-masks real exceptions).
- **The converter must serve both token species** — human tokens and SA tokens differ in claims present, not just values.
- **IDE-vs-build disputes, diagnostic ladder:** jar on classpath (mind `(c)` = constraint, not dependency) → file in right module → `gradlew compileKotlin` is the verdict → sync → invalidate caches. Never debug code while Gradle is green and only the editor complains.

## Parked

- Tests (plan task 2.6): `spring-security-test` `jwt()` post-processor + one real-Keycloak issuer-handshake test.
- `hasRole('system')` guard on `POST /api/users` — Phase 4.2.
