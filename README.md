# Quarkus RBAC Template

[中文](README.zh-CN.md)

A reusable Quarkus backend foundation with RBAC (Role-Based Access Control), JWT authentication, and DDD-style architecture.

## Features

- **Security**: Quarkus Security, JWT, refresh token, provider-chain login orchestration
- **RBAC**: User, Role, Permission, PermissionGroup with Infinispan-cached permission snapshots
- **Persistence**: Hibernate ORM + Panache, Blaze-Persistence + Entity View for queries
- **DDD**: Bounded-context modules (identity, accesscontrol, audit)
- **Code Quality**: Checkstyle, SpotBugs, JaCoCo, OWASP Dependency Check, Spotless

## Quick Start

### Prerequisites

- JDK 25+
- PostgreSQL
- Infinispan (optional for dev, required for production; Dev Services auto-starts with Docker)

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
├── cache               # Refresh token, authority version, login attempt state (Infinispan)
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
| `generateRsaKeys` | Generate RSA key pair (privateKey.pem, publicKey.pem). `-PoutputDir=...` optional |

See [buildSrc/README.md](buildSrc/README.md) for details.

## License

See repository for license information.
