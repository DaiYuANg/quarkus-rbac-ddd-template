# Migration Notes# Migration Notes

## v14 highlights
- Security login providers now return `SUCCESS / ABSTAIN / FAILURE`, which fixes the earlier problem where `config-user` could block `db-user` from ever being attempted.
- Added default authentication success/failure handlers plus token/refresh abstractions in `libs/security`.
- Added `AdminRefreshTokenAuthenticationProvider`, so refresh-token processing is now part of the same authentication chain.
- Introduced `BaseQueryRepository`; moved the four admin list repositories onto Blaze + QueryDSL (strongly-typed Q-types). `BaseBlazeQueryRepository` / `BaseEntityViewQueryRepository` removed after BlazeJPAQuery refactor.
- Permission and permission-group list queries now also use Blaze Entity Views instead of mixed constructor-projection fallbacks.

# Migration Notes v13

## Security

The previous JWT-centric login service has been reworked toward a reusable security module.

### New pieces

- `libs/security`
- `LoginAuthenticationRequest`
- `LoginAuthenticationProvider`
- `LoginAuthenticationManager`
- `ConfigUserAuthenticationProvider`
- `DbUserAuthenticationProvider`
- `AuthenticatedUser`

### Why

This keeps Quarkus Security in place for request authentication/authorization, while moving business login orchestration into a provider-chain design that can support:

- DB users
- configuration-backed bootstrap users
- refresh token flows
- future API key / OIDC / external identity providers

## Query stack

The project now converges on:

- Hibernate ORM + Panache for command-side repository work
- Blaze-Persistence for complex read-side queries
- Hibernate Processor / JPA static metamodel for sort and field safety

### Removed from main direction

- OpenFeign Querydsl (removed from main path)
- Blaze Querydsl (removed from main path) integration

### Added

- `MetamodelUserQueryBuilder`
- `MetamodelRoleQueryBuilder`
- `MetamodelPermissionQueryBuilder`
- `MetamodelPermissionGroupQueryBuilder`
- `MetamodelSortMapping`
- `MetamodelSorts`

## Practical note

This archive is meant as the next iteration artifact, not a claim of fully verified local compilation in this environment. The purpose of v13 is to lock the architecture onto the agreed direction so your local build can continue from a cleaner base.

## v15 highlights

- `libs/security` now supports deterministic provider ordering via `@Priority` / `order()`.
- Added `CurrentAuthenticatedUser` bridge so request-scoped code can resolve richer user context from `SecurityIdentity`.
- Added `AuthenticationProviderRegistry` and `CurrentUserAccess` helper components for framework-style reuse.
- Added `BaseCommandRepository` and `BasePanacheCommandRepository` so write-side repositories start converging on one strong-typed command abstraction.

## v16
- 安全模块新增 `RequestMetadataProvider/Access`、`AuthenticatedPrincipalResolver`、`ActorAuditor`。
- 登录日志与操作日志默认从请求头中提取 `X-Forwarded-For` / `User-Agent` / `X-Request-Id`。
- 审计字段从纯 username 升级为 `userType:username` actor key。
- repository/query 基类新增 `BaseRepositorySupport` 与 `BasePageQueryRepository`。


## v17
- Added token-context abstractions and principal attribute serialization to stabilize SecurityIdentity <-> business principal mapping.
- Added `AuthenticationProviders` facade to make ordered provider resolution reusable.
- Added `AuditSnapshotProvider` for Blaze/audit plumbing. `BaseEntityViewQueryRepository` removed after BlazeJPAQuery refactor.


## v18 migration process split

- Added a dedicated `apps/migrator` Quarkus application for Flyway execution.
- Migrations live under `apps/migrator/src/main/resources/db/migration`.
- `admin-api` no longer auto-creates schema or loads bootstrap SQL through Hibernate.
- Deployment order becomes: run migrator once, then deploy `admin-api`.
- This keeps schema evolution explicit and avoids repeated migration work on every API pod restart.

## v19

- Added a dedicated command-driven migrator runtime in `apps/migrator`.
- Added profile-aware datasource examples for `dev`, `test`, and `prod`.
- Simplified migrator to standard Quarkus startup with Flyway executing at application start.
- Removed custom migrator command parsing and custom entrypoint scripts in favor of configuration-driven behavior.
- Kept `admin-api` on schema `validate` mode so migration remains a separate deployment concern.


## v21 Flyway alignment with the original Spring template

This round focuses only on database migration alignment.

Added migrations (consolidated into sequential versions):
- `V1__Init_RBAC.sql` (merged: schema + bootstrap data, English)
- `V1_1__Audit_Log_Tables.sql`
- `V1_2__Job_Execution_History.sql`
- `V1_3__Quartz_Admin_Tables.sql`

What was aligned (now in V1__Init_RBAC.sql):
- RBAC schema includes Spring-template-style audit/support columns (`version`, `sort`, `deleted`, `avatar`, `latest_change_password`).
- Added `job_execution_history` so the schema is closer to the original scheduler/history footprint.
- Added Quartz admin JDBC tables under the `qrtz_admin_*` prefix.
- Added comments and indexes closer to the original schema intent.

What was intentionally **not** forced to 1:1:
- Enum/status columns remain compatible with the current Quarkus entities.
- The current Quarkus join-table naming stays unchanged where the runtime mapping already depends on it.
- TimescaleDB-specific hypertable/compression statements were not copied blindly, because they would make PostgreSQL-only startup more fragile.

So this round is a **best-effort schema convergence**, not a blind checksum-copy of the Spring project.


## Permission code structure

- Permission query/VO paths now return these structured fields directly.


## Authorization cache and Infinispan snapshot

This template uses Infinispan as the runtime permission snapshot cache.

- Login and refresh publish an effective permission snapshot.
- Request authentication re-hydrates permissions from Infinispan and only falls back to the database when the cached authority version is stale or missing.
- Endpoint annotations remain available, but complex permission decisions should be delegated to `AuthorizationService`.
