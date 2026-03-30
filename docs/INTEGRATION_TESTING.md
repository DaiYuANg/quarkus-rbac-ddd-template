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

`AdminApiFullStackFlowIT` covers **super-admin login → JWT → `/me`** and **liveness**. `MobileApiFullStackFlowIT` is now a user-side smoke test and no longer assumes a built-in mobile config account.

**Refresh tokens** are not covered here: `AdminRefreshTokenAuthenticationProvider` currently reloads the principal from **`UserRepository`** only. Add a separate IT with a seeded `sys_user` row if you need refresh coverage.

Run:

```bash
./gradlew :apps:admin-api:test --tests "*FullStackFlowIT*"
./gradlew :apps:mobile-api:test --tests "*FullStackFlowIT*"
```

**Requirements**: Docker (or compatible runtime) available for Testcontainers.

## Contract vs full-stack

- **`AdminIdentityRestJsonContractTest`** / **`MobileIdentityRestJsonContractTest`**: mock `AuthApplicationService` to lock **JSON field names**.
- **`*FullStackFlowIT`**: real services, Redis, DB — catches wiring and security regressions.

## Optional: test against a manually running app

For exploratory checks, start Postgres + Redis + **`migrator`**, then **`quarkusDev`**, and call the same endpoints with curl or REST clients. That path is not automated in this repo; prefer **`@QuarkusTest`** flows above for repeatable CI.
