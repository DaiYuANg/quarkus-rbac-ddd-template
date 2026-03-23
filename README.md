# Quarkus RBAC Template

[中文](README.zh-CN.md)

A reusable Quarkus backend foundation with RBAC (Role-Based Access Control), JWT authentication, and DDD-style architecture.

## Features

- **Security**: Quarkus Security, JWT, refresh token, provider-chain login orchestration
- **RBAC**: User, Role, Permission, PermissionGroup with Valkey-cached permission snapshots
- **Persistence**: Hibernate ORM + Panache, Blaze-Persistence + Entity View for queries
- **DDD**: Bounded-context modules (identity, accesscontrol, audit)
- **Code Quality**: Checkstyle, SpotBugs, JaCoCo, OWASP Dependency Check, Spotless

## Quick Start

### Prerequisites

- JDK 21+ or JDK 25
- PostgreSQL
- Valkey/Redis (optional for dev, required for production; use docker-compose or local install)

**Local config**: Copy `gradle.properties.template` to `gradle.properties` and adjust for your machine (JVM args, proxy, etc.). `gradle.properties` is gitignored.

**JWT keys**: Run `./gradlew generateRsaKeys` before first start. Keys are generated to the project root and gitignored.

**Default config user** (no DB): `root` / `root` — from `app.security.config-users`, for quick API testing.

### Run Migrations

```bash
./gradlew :apps:migrator:quarkusRun
```

### Start Admin API

```bash
./gradlew :apps:admin-api:quarkusDev
```

## Project Structure

```
libs/
├── common              # Result, exceptions, PageQuery
├── persistence         # BaseEntity, audit listener, base repos, query base classes
├── accesscontrol       # Role, Permission, PermissionGroup (RBAC)
├── identity            # User (depends on accesscontrol)
├── audit               # OperationLog, LoginLog
├── cache               # Refresh token, authority version, login attempt state (Valkey/Redis)
└── security            # Auth provider chain, JWT, current user, token context

apps/
├── admin-api           # Admin REST API
└── migrator            # Flyway migration (one-shot)
```

## Configuration

- **dev**: Local PostgreSQL defaults
- **test**: `rbac_test` database
- **prod**: `DB_JDBC_URL`, `DB_USERNAME`, `DB_PASSWORD` from environment

## Documentation

| Document | English | 中文 |
|----------|---------|------|
| DDD Architecture | [ARCHITECTURE_DDD.md](docs/ARCHITECTURE_DDD.md) | [ARCHITECTURE_DDD.zh-CN.md](docs/ARCHITECTURE_DDD.zh-CN.md) |
| Authorization Flow | [AUTHORIZATION_FLOW.md](docs/AUTHORIZATION_FLOW.md) | [AUTHORIZATION_FLOW.zh-CN.md](docs/AUTHORIZATION_FLOW.zh-CN.md) |
| Production Checklist | [PRODUCTION_READINESS_CHECKLIST.md](docs/PRODUCTION_READINESS_CHECKLIST.md) | [PRODUCTION_READINESS_CHECKLIST.zh-CN.md](docs/PRODUCTION_READINESS_CHECKLIST.zh-CN.md) |
| Troubleshooting | [TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) | [TROUBLESHOOTING.zh-CN.md](docs/TROUBLESHOOTING.zh-CN.md) |

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
