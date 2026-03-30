# Quarkus RBAC Template

[中文](README.zh-CN.md)

A reusable Quarkus backend foundation with RBAC (Role-Based Access Control), JWT authentication, and DDD-style architecture.

## Features

- **Security**: Quarkus Security, JWT, refresh token, provider-chain login orchestration
- **RBAC**: User, Role, Permission, PermissionGroup with Valkey-cached permission snapshots
- **Persistence**: Hibernate ORM + Panache, Blaze-Persistence + Entity View for queries
- **DDD**: Bounded-context **modules** (`identity`, `accesscontrol`, `security-runtime`, optional `example-ddd`) plus shared **libs**
- **Apps**: **admin-api** (management REST), **mobile-api** (C-side REST, separate routes and principal typing via `app.identity.*`)
- **Cross-cutting**: **libs:rest-support** (shared JAX-RS exception mapping, refresh-token cookies), optional **@ReplayProtected** (timestamp + nonce, Redis)
- **Code Quality**: Checkstyle, SpotBugs, JaCoCo, OWASP Dependency Check, Spotless; `-Xlint:deprecation` on compile

## Quick Start

### Prerequisites

- **JDK 25** (Gradle toolchain; the codebase uses JDK 25 language features such as flexible constructor bodies)
- PostgreSQL
- Valkey/Redis (optional for dev, required for production; use docker-compose or local install)

**Local config**: Copy `gradle.properties.template` to `gradle.properties` and adjust for your machine (JVM args, proxy, etc.). `gradle.properties` is gitignored.

**JWT keys**: Run `./gradlew generateRsaKeys` before first start. Keys are generated to the project root and gitignored.

**Default super-admin** (no DB): `root` / `root` — from `app.security.super-admin`, for quick admin API testing.

### Run Migrations

Apply Flyway scripts **before** starting APIs when using `hibernate-orm.schema-management.strategy: validate`:

```bash
./gradlew :apps:migrator:quarkusRun
```

### Start Admin API

```bash
./gradlew :apps:admin-api:quarkusDev
```

Default HTTP: `http://localhost:8080` (see `apps/admin-api/src/main/resources/application.yaml`).

### Start Mobile API (sample process)

```bash
./gradlew :apps:mobile-api:quarkusDev
```

Default HTTP: `http://localhost:8081`. Uses the same database and Redis as admin unless you override config. The mobile process no longer ships with built-in config accounts.

## Project Structure

```
libs/
├── common              # Result, exceptions, PageQuery
├── persistence         # BaseEntity, audit listener, base repos, query base classes
├── accesscontrol       # Role, Permission, PermissionGroup (RBAC)
├── identity            # User (depends on accesscontrol)
├── audit               # OperationLog, LoginLog
├── cache               # Refresh token, authority version, login attempts, permission catalog, replay nonces (Redis)
├── security            # Auth provider chain, JWT, current user, principal definitions, config
└── rest-support        # Shared GlobalExceptionHandler, RefreshTokenCookies (JAX-RS)

modules/
├── identity            # Auth application service, profile, ports
├── accesscontrol       # User/role/permission application services, permission catalog loader
├── security-runtime    # Quarkus wiring: DB/super-admin login, JWT issue, permission augmentor, replay filter
└── example-ddd         # Sample product/order bounded context (ports + Panache adapters)

apps/
├── admin-api           # Management REST (identity, accesscontrol, example endpoints)
├── mobile-api          # C-side REST (auth + session sample; no admin CRUD)
└── migrator            # Flyway migration (one-shot)
```

## Configuration

- **dev**: Local PostgreSQL / Redis defaults per `application.yaml` in each app
- **test**: Testcontainers PostgreSQL + Redis dev services where enabled
- **prod**: datasource and Redis from environment (see `docker-compose-prod.yaml` for an example)

Notable keys:

- **`app.identity.*`**: principal `userType` written for DB-backed users (differs between admin-api and mobile-api)
- **`app.replay.*`**: replay protection for endpoints annotated with `@ReplayProtected` (admin example APIs)

## Documentation

| Document | English | 中文 |
|----------|---------|------|
| Project design (DDD, diagrams, stack) | [PROJECT_DESIGN.md](docs/PROJECT_DESIGN.md) | [PROJECT_DESIGN.zh-CN.md](docs/PROJECT_DESIGN.zh-CN.md) |
| Security runtime (auth chain, snapshot flow, Mermaid diagrams) | [SECURITY_RUNTIME.md](docs/SECURITY_RUNTIME.md) | [SECURITY_RUNTIME.zh-CN.md](docs/SECURITY_RUNTIME.zh-CN.md) |
| Mobile API (dependency whitelist) | [MOBILE_API.md](docs/MOBILE_API.md) | [MOBILE_API.zh-CN.md](docs/MOBILE_API.zh-CN.md) |
| DDD Architecture | [ARCHITECTURE_DDD.md](docs/ARCHITECTURE_DDD.md) | [ARCHITECTURE_DDD.zh-CN.md](docs/ARCHITECTURE_DDD.zh-CN.md) |
| Authorization Flow | [AUTHORIZATION_FLOW.md](docs/AUTHORIZATION_FLOW.md) | [AUTHORIZATION_FLOW.zh-CN.md](docs/AUTHORIZATION_FLOW.zh-CN.md) |
| Production Checklist | [PRODUCTION_READINESS_CHECKLIST.md](docs/PRODUCTION_READINESS_CHECKLIST.md) | [PRODUCTION_READINESS_CHECKLIST.zh-CN.md](docs/PRODUCTION_READINESS_CHECKLIST.zh-CN.md) |
| Troubleshooting | [TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) | [TROUBLESHOOTING.zh-CN.md](docs/TROUBLESHOOTING.zh-CN.md) |

Index: [docs/README.md](docs/README.md)

## Build Tasks

### Code Quality

```bash
./gradlew check                    # Checkstyle, SpotBugs, tests
./gradlew spotlessApply            # Format code
./gradlew dependencyCheckAnalyze   # OWASP vulnerability scan
```

### buildSrc Tasks

| Task | Description |
|------|-------------|
| `replacePackage` | Batch-replace package names and group. `-PfromPackage=... -PtoPackage=...` |
| `generateRsaKeys` | Generate JWT RSA keys (project root). Run before first start. `-PoutputDir=...` optional |

See [buildSrc/README.md](buildSrc/README.md) for details.

## License

See repository for license information.
