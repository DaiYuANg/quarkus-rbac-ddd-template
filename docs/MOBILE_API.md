# Mobile API process (`apps:mobile-api`)

[中文](MOBILE_API.zh-CN.md)

The mobile (C-side) Quarkus app is a **separate deployment unit** from `admin-api`. It reuses **identity** and **security-runtime** but deliberately **does not** depend on RBAC administration modules, so consumer-facing HTTP cannot accidentally call user/role/permission management code paths.

## Dependency whitelist (Gradle)

These are the **intended** `implementation` dependencies in `apps/mobile-api/build.gradle.kts`:

| Dependency | Role |
|------------|------|
| `libs:common` | `Result`, shared errors, pagination helpers |
| `libs:rest-support` | Shared `GlobalExceptionHandler`, refresh-token cookie helpers |
| `modules:identity` | Login, profile, `AuthApplicationService`, DTOs |
| `modules:security-runtime` | JWT pipeline, Quarkus security wiring, login orchestration hooks |
| Quarkus bundles | REST, Hibernate (via transitive needs of above), observability, security extensions |

**Do not add** without an explicit architectural decision:

- `modules:accesscontrol` — would expose RBAC administration services to the mobile classpath.
- `modules:example-ddd` — sample domain; keep it admin-only unless you intend a public catalog API.

If you need new capabilities for mobile:

1. Prefer extending **`modules:identity`** (new use cases / ports) or adding a **new bounded-context module** that does not pull admin CRUD.
2. Add **`implementation(projects.modules.yourContext)`** in `mobile-api` only if that module’s public API is safe for the C-side threat model.

## Configuration differences

- **`app.identity.db-user-type` / `config-user-fallback-type`** — JWT `userType` for members vs admins (`MEMBER` vs `ADMIN` in the template).
- **HTTP port** — default `8081` vs admin `8080`.
- **Cookie path** — `/api/mobile/v1/auth` so refresh cookies do not collide with admin (`/api/v1/auth`).

## Contract tests

JSON shape for login and `/me` is covered by `MobileIdentityRestJsonContractTest` (REST Assured). Keep field names and `Result` envelope stable or version the API.
