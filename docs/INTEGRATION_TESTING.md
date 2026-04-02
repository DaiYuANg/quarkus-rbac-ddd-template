# Integration and full-stack tests

[中文](INTEGRATION_TESTING.zh-CN.md)

## In-JVM “app is up” tests (recommended for CI)

The template uses **`@QuarkusTest`** with:

- **PostgreSQL** via Testcontainers JDBC (`jdbc:tc:postgresql:16-alpine:///…`)
- **Valkey** via **`ValkeyTestResource`** (Testcontainers), wired with **`QuarkusPostgresValkeyTestProfile`**
- **Hibernate `drop-and-create`** so the schema matches entities without running **`migrator`** first
- **`quarkus.management.enabled=false`** in the shared test profile so **`/q/health/live`** is served on the test HTTP port (production YAML uses a separate management port)

**Full-stack flow tests** (no mocks on `AuthApplicationService`):

| App | Class |
|-----|--------|
| admin-api | `com.github.DaiYuANg.integration.AdminApiFullStackFlowIT` |
| mobile-api | `com.github.DaiYuANg.mobile.integration.MobileApiFullStackFlowIT` |

`AdminApiFullStackFlowIT` currently covers:

- `root` (super-admin) login, JWT issuance, `/me`, and `/q/health/live`
- `root` provisioning users, roles, permission groups, and permission assignments
- permission expansion and permission revocation while reusing the **same access token**
- token invalidation semantics after disabling a user
- password-change semantics: old refresh token revoked, old password rejected, new password accepted, and the already-issued access token still usable until expiry
- admin paging contract: zero-based `page` / `size` on HTTP input and raw `toolkit4j` `PageResult` fields on output (`content / page / size / totalElements / totalPages`)
- admin permission catalog test seed loaded from `apps/admin-api/src/test/resources/import-test.sql` as the single source for full-stack test permissions

`MobileApiFullStackFlowIT` is now a user-side smoke test and no longer assumes a built-in mobile config account.

Run:

```bash
./gradlew :apps:admin-api:test --tests "*FullStackFlowIT*"
./gradlew :apps:mobile-api:test --tests "*FullStackFlowIT*"
```

**Requirements**: Docker (or compatible runtime) available for Testcontainers.

## Contract vs full-stack

- **`AdminIdentityRestJsonContractTest`** / **`MobileIdentityRestJsonContractTest`**: mock `AuthApplicationService` to lock **JSON field names**.
- **`PageQueryCompatibilityTest`** / **`PageResultCompatibilityTest`**: lock the admin paging contract so `pageNum/pageSize` aliases or custom page wrappers do not reappear silently.
- **`*FullStackFlowIT`**: real services, Redis, DB — catches wiring and security regressions.

## Optional: test against a manually running app

For exploratory checks, start Postgres + Redis + **`migrator`**, then **`quarkusDev`**, and call the same endpoints with curl or REST clients. That path is not automated in this repo; prefer **`@QuarkusTest`** flows above for repeatable CI.
