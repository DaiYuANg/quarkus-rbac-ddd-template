# Quarkus RBAC Template v19

This template is converging toward a reusable Quarkus backend foundation instead of a one-off migration demo.

## Core direction

- Quarkus Security as the security runtime foundation
- Lightweight provider-chain login/authentication orchestration in `libs/security`
- Hibernate ORM + Panache for write-side persistence
- Blaze-Persistence + Entity View for read-side list queries
- Hibernate Processor / static metamodel as the strong-typed query field path direction
- Flyway managed schema evolution through a dedicated `apps/migrator` process
- Fesod-backed export SPI with CSV fallback

## Modules

- `apps/admin-api` — admin REST API
- `apps/migrator` — one-shot Flyway migration process
- `libs/common` — result models, exceptions, shared primitives
- `libs/persistence` — BaseEntity, audit listener, base repos, query base classes, PageSlice, QuerySort, etc.
- `libs/accesscontrol` — Role, Permission, PermissionGroup, their entities, repos, Blaze query repos
- `libs/identity` — User, UserRepository, UserQueryRepository (depends on accesscontrol for Role)
- `libs/audit` — OperationLog, LoginLog, their repos, AuditSnapshotProvider
- `libs/redis` — refresh token, authority version, login attempt state
- `libs/export` — export SPI / response models
- `libs/security` — provider chain, current user, token/principal context

## Security direction

The frontend still uses a single login surface such as `/api/v1/auth/login`, but internally the flow now routes through:

- `LoginAuthenticationManager`
- ordered `LoginAuthenticationProvider`s
- `DbUserAuthenticationProvider`
- `ConfigUserAuthenticationProvider`
- `AdminRefreshTokenAuthenticationProvider`

Quarkus Security still owns request-time authenticated identity and authorization. The custom security module owns business-level login orchestration.

## Query direction

The query path is intentionally converging to:

- Blaze-Persistence for execution
- Entity Views for read projections
- Hibernate Processor / static metamodel for field references and sorting support

The old Querydsl-main-path direction has been removed from the primary architecture.

## Flyway and migrator

Schema creation and bootstrap seed data are managed by `apps/migrator`, not by `admin-api`.

### Common local commands

Run migrations once:

```bash
./gradlew :apps:migrator:quarkusRun
```

Run with an explicit Flyway command:

```bash
./gradlew :apps:migrator:quarkusRun --args='validate'
./gradlew :apps:migrator:quarkusRun --args='info'
./gradlew :apps:migrator:quarkusRun --args='repair'
./gradlew :apps:migrator:quarkusRun --args='baseline'
```

Then start the API:

```bash
./gradlew :apps:admin-api:quarkusRun
```

### Environment / profile direction

- `dev` uses local PostgreSQL defaults
- `test` points to `rbac_test`
- `prod` reads JDBC credentials from `DB_JDBC_URL`, `DB_USERNAME`, `DB_PASSWORD`

### Container usage

Use the module Dockerfile directly. No custom entrypoint is required.

`admin-api` runs with schema validation only, so startup fails fast when the Flyway baseline has not been applied.

## Recent infrastructure highlights

- **v24 DDD refactor**: `libs/dbms` dissolved into bounded-context modules—`libs/persistence` (shared base), `libs/identity` (User), `libs/accesscontrol` (Role, Permission, PermissionGroup), `libs/audit` (OperationLog, LoginLog). See `docs/ARCHITECTURE_DDD.md`.
- v14: provider chain changed to `SUCCESS / ABSTAIN / FAILURE`, so config-user and db-user can coexist sanely.
- v15: provider ordering, richer current user bridge, and base command repository abstraction were added.
- v16: request metadata, principal resolver, actor-based audit keys, and shared page/query support were added.
- v17: token context, principal attribute serialization, ordered authentication provider facade, and entity-view query support were added.
- v18: Flyway and the dedicated `apps/migrator` one-shot process were introduced.
- v19: dedicated `apps/migrator` module was added for standalone Flyway execution.

## Migrator usage

For local development, run the migrator directly with Quarkus:

```bash
./gradlew :apps:migrator:quarkusDev
```

If you need different Flyway behavior, change it through `apps/migrator/src/main/resources/application.properties` or profile-specific configuration.

## Container images

- admin-api: `apps/admin-api/src/main/docker/Dockerfile.jvm`
- migrator: `apps/migrator/src/main/docker/Dockerfile.jvm`

Recommended deployment order:

1. run migrator once
2. start admin-api after migration succeeds

## v21 schema alignment note

This iteration only tightens Flyway schema alignment against the original Spring Boot template.

Added migrations:
- `V1_3__Align_RBAC_Schema_With_Spring_Template.sql`
- `V1_4__Job_Execution_History.sql`
- `V1_5__Quartz_Admin_Tables.sql`

Important constraints kept on purpose:
- current Quarkus enum/status mappings were preserved
- current join-table names used by the Quarkus entities were preserved
- TimescaleDB-only DDL from the Spring project was not copied directly


## v23 code-style cleanup

- controller/service/security 的主干类继续收成 **Lombok 构造器注入**
- 导出链路新增 **MapStruct `ExportMapper`**，去掉 resource 里的手写 `new ExportRow(...)`
- 继续减少字段注入，统一依赖装配风格

## Build and observability baseline

- Gradle Wrapper is included and pinned to Gradle 9.3.1.
- Dependency management is centralized in `gradle/libs.versions.toml` with explicit `group` + `name` (+ `version` when needed) coordinates.
- The Quarkus BOM is exposed as `libs.quarkus.bom` and imported with `platform(...)`.
- `admin-api` includes SmallRye Health, Info, Micrometer + Prometheus, and OpenTelemetry OTLP exporter dependencies.


## Build script notes

- The build now uses Gradle version catalog bundles so application modules can depend on `libs.bundles.quarkus.application`, `libs.bundles.quarkus.observability`, and `libs.bundles.quarkus.security.application` instead of listing every Quarkus extension one by one.
- Root build logic keeps using the root catalog explicitly in shared `subprojects {}` configuration to avoid Kotlin DSL accessor issues in Gradle 9.x.


## Authorization and permission snapshot

The template now keeps the runtime authorization path split into two layers:

- Quarkus Security remains the entrypoint for request authentication and coarse endpoint protection.
- Effective permissions are aggregated once, cached in Redis as a permission snapshot, and re-hydrated into `SecurityIdentity` through `AdminPermissionSecurityIdentityAugmentor`.

Key pieces:

- `PermissionSnapshotStore` stores effective permissions in Redis.
- `PermissionSnapshotLoader` loads the authoritative snapshot from the database.
- `AuthorizationService` is the unified runtime permission evaluator for dynamic checks.

Recommended usage:

- Keep `@PermissionsAllowed` for stable, coarse-grained endpoint gates.
- Use `AuthorizationService.check(code)` or `AuthorizationService.check(domain, resource, action)` inside service methods for complex business authorization.
- Let `authorityVersion` invalidate stale permission snapshots instead of querying the database on every request.


## Authorization verification focus
- login publishes permission snapshots to Redis
- authority version changes invalidate stale snapshots
- endpoint guards remain lightweight; complex authorization lives in services
- denied authorization attempts are auditable


## Configuration

- `admin-api` and `migrator` now use `application.yaml` instead of `application.properties`.
- JSON console logging is enabled by default and disabled in `dev` / `test` profiles.
