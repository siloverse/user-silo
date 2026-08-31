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

## Lessons paid for (Phase 4.3 integration, 2026-08-26) — the save() routing saga

- **`save()` routes by `isNew()`, and default `isNew()` is `id == null`.** Non-null (app-assigned) id → `merge`: Hibernate SELECTs, then reflectively **instantiates** a managed copy → needs the no-arg ctor → `InstantiationException` (hit live). Null id with no `@GeneratedValue` → `persist` → `IdentifierGenerationException: must be manually assigned` (also hit live). Both faces of one mechanism, now understood.
- **Fix applied:** `kotlin("plugin.jpa")` in silo/build.gradle.kts (versionless resolution off the convention classpath FAILED — `kotlin-noarg` is a separate artifact the convention doesn't carry; resolved from portal pinned to Kotlin 2.4.0). Convention migration parked for siloverse-build. Entity id restored to app-assigned as ctor default `val id: UUID = UUID.randomUUID()` (no-arg plugin does NOT run initializers on hydration, so loads don't waste a UUID).
- **Accepted cost, journaled:** assigned id ⇒ merge path ⇒ SELECT-before-INSERT on every create. Upgrades parked: `Persistable<UUID>` (Kotlin caveat: `getId()` platform-clash with the `id` property getter — private ctor param + explicit override) or `@GeneratedValue` (reverses the app-assigned decision; only on purpose).
- Lombok non-answer, for the record: Lombok is a javac plugin — inert on Kotlin sources; kotlin-jpa/no-arg IS the Kotlin answer, and its synthetic ctor is invisible to Kotlin callers (better than defaults hack, which makes `UserEntity()` a legal nonsense expression).

## Built & green (Phase 4.1 + 4.2, 2026-08-21/22) — persistence + guarded write endpoint

- **Schema handling is ROUTE B — explicit everywhere (revised 2026-08-22, supersedes the search_path design):** migration DDL says `user_silo.users`, the entity says `@Table(schema = "user_silo")`, Flyway says `default-schema: user_silo`. The role's pinned `search_path` was REMOVED from Puppet (YAGNI — nothing consults it; security was never its job: ownership+USAGE grants are the wall, search_path is only name resolution). All three schema-aware components state the name openly; none rely on connection ambience.
- **Flyway migrates on startup** into `siloverse.user_silo` (verified after a full DB rebuild from code): id uuid pk app-assigned, keycloak_id unique, email unique, display_name, timestamptz created/updated. `ddl-auto: validate` — Flyway owns the schema, Hibernate only checks it.
- **`POST /api/users` guarded by `hasRole('SYSTEM')` — the role model's first real gate.** Verified matrix 2026-08-22: SA token → 201 + row; customer token → 403 (Keycloak role → claim → converter → rule, every layer firing); no token → 401; duplicate keycloakId → 409 (app check + DB unique as backstop). Contract DTOs in the `web` module — the jar auth-silo will consume in 4.3.
- **Integration tests hermetic via Testcontainers**: `TestcontainersConfiguration` bean + `@ServiceConnection` (container-derived `ConnectionDetails` outrank the yml datasource). Test parity for route B comes free: Flyway's `createSchemas` default auto-creates `user_silo` in the blank container (permitted there — superuser), while on the VM the same willingness is blocked by grants — the "environment grants the space" layering holds by *permission*, not configuration.
- **Boot 4 rule, third occurrence, now law:** a bare technology library integrates NOTHING — Boot 4's modular auto-configuration lives in `spring-boot-{starter-}X` modules. flyway-core alone = inert; `spring-boot-starter-flyway` + `flyway-database-postgresql` (runtimeOnly) is the pair. (Previous occurrences: oauth2-resource-server module, webmvc-test.)
- Layering principle (journaled during the schema redesign): **the environment grants the space, the application fills it** — Puppet creates db/role/schema/grants, Flyway creates tables.

## Lessons paid for (Phase 4, 2026-08-21/22)

- **Kotlin entity needs a no-arg constructor only on READ** — persist works (you construct), first `findById` throws `No default constructor` (Hibernate constructs). Predicted before it fired. Fix now: defaults on all ctor params (synthetic no-arg); proper fix parked: `kotlin-jpa` compiler plugin belongs in the siloverse-build convention (version must track Kotlin).
- **Jackson 2 vs 3 — same class name, different package, unrelated types.** Boot 4 wires `tools.jackson.databind.ObjectMapper`; autowiring `com.fasterxml.…ObjectMapper` finds no bean (those jars are transitive stragglers). Rule: check the package on every Jackson import. Convention implication parked: siloverse-build ships the Jackson-2 kotlin module, which registers into nothing Boot 4 manages.
- **Jackson vs Hibernate instantiate opposite ways:** Jackson binds Kotlin primary constructors via parameter names (`javaParameters` flag); Hibernate demands no-arg + field writes. Same DTO shape, two frameworks, two strategies.
- **Check the actual inputs before predicting:** two predictions lost to unread code (schema-qualified DDL, schema-pinned entity) — the arbiter cuts both ways.

## Tests (2.6) — green 2026-08-20, three layers with distinct claims

- **Converter unit tests** (pure, no Spring; `Jwt.withTokenValue` builder): happy path + the two regressions (no `realm_access` → empty authorities; no `preferred_username` → name falls back to sub). Today's incidents frozen as assertions.
- **`jwt()` MockMvc tests** (`SecurityConfigurationIntegrationTest`): the post-processor injects a ready-made Authentication — proves the RULES (401 anonymous / 200 authenticated / claims reach controller), NOT the decoder or converter wiring. Authorities passed explicitly — honest about what's covered. Boot 4 note: `AutoConfigureMockMvc` moved to `spring-boot-webmvc-test` (`org.springframework.boot.webmvc.test.autoconfigure`) — starter-test no longer brings technology test modules.
- Suite passes with Keycloak down — decoder is a lazy supplier; issuer contacted on first VALIDATION, not startup.

## Phase 2 Reflect (answered 2026-08-20)

- 401 origin: anonymous → denied at `AuthorizationFilter` (bare challenge); bad token → `BearerTokenAuthenticationFilter` (`error="invalid_token"`).
- No per-request Keycloak call: local signature check against cached JWKS; the one exception is unknown `kid` after key rotation → refetch (observed live after the realm rebuild).
- Opaque+introspection trade: buys instant revocation + no claim leakage from leaked tokens; costs a network hop per request and puts the AS in every request's hot path (AS down = API down). Industry hybrid = short-lived JWTs (our 300s) + refresh tokens → revocation delay bounded by access-token lifetime.

## Parked

- One real-Keycloak integration test (issuer handshake / decoder wiring) — the only untested layer.
- `hasRole('system')` guard on `POST /api/users` — Phase 4.2.
