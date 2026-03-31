# Production Architecture Layout

[中文](ARCHITECTURE_DDD.zh-CN.md)

For **Mermaid architecture diagrams** (system context, Gradle layers, request flow), benefits, evolution notes, and the full technology matrix, see [PROJECT_DESIGN.md](PROJECT_DESIGN.md).

The codebase separates **shared kernel** (`libs`), **application / use-case modules** (`modules`), and **deployable adapters** (`apps`). REST resources and HTTP query binding live in each app; domain logic, use cases, and pure read-query models sit in `modules` and `libs`.

Read this layout as a **production-oriented Quarkus modular monolith** with a **CQRS-lite** split. Identity and accesscontrol are packaged separately, but today they still form one RBAC core rather than two fully independent bounded contexts.

## Layering: libs → modules → apps

```
libs/                    # Shared kernel (entities, repos, security primitives, Redis helpers)
modules/                 # Application services, ports, and Quarkus-oriented wiring per context
apps/                    # JAX-RS entrypoints, DTOs next to resources, process-specific config
```

## Repository layout

```
libs/
├── common              # Result, exceptions, PageQuery
├── persistence         # BaseEntity, audit listener, repository/query base classes
├── accesscontrol       # RBAC: Role, Permission, PermissionGroup
├── identity            # User (depends on accesscontrol for roles)
├── audit               # OperationLog, LoginLog
├── cache               # Refresh tokens, authority version, login attempts, catalog cache, replay nonces
├── security            # Quarkus security integration, JWT helpers, principal definitions, config
└── rest-support        # Shared JAX-RS: GlobalExceptionHandler, RefreshTokenCookies

modules/
├── identity            # Auth application service, user profile, ports (uses libs above)
├── accesscontrol       # User/role/permission application services, permission catalog loader
├── security-runtime    # Quarkus wiring: DB/super-admin login, JWT issue, permission augmentor, replay filter
└── example-ddd         # Sample product/order bounded context (ports + Panache adapters)

apps/
├── admin-api           # Management REST under com.github.DaiYuANg.modules.* (+ example resources)
├── mobile-api          # C-side REST under com.github.DaiYuANg.mobile.identity.* (auth/session sample)
└── migrator            # Flyway (run before APIs when schema strategy is validate)
```

## admin-api package layout (driving adapters)

REST classes and request DTOs sit beside each other under `modules` subpackages (not a separate `api/controller` tree):

```
com.github.DaiYuANg.modules
├── identity              # AuthResource, MeResource, LoginAuditEventObserver
├── accesscontrol         # UserResource, RoleResource, Permission*, forms
└── example               # ExampleProductResource, ExampleOrderResource (example-ddd)
```

Shared HTTP concerns (`GlobalExceptionHandler`, refresh-token cookie helpers) come from **`libs:rest-support`** and are registered via CDI when the app depends on that library.

## mobile-api package layout

A second process with its own path prefix and `app.identity.*` typing for JWT principals:

```
com.github.DaiYuANg.mobile.identity
├── MobileAuthResource
├── MobileMeResource
├── MobileSessionResource
├── MobilePrincipalView
└── LoginAuditEventObserver
```

It depends on **`modules:identity`** and **`modules:security-runtime`** (not full accesscontrol CRUD).

## Gradle dependency sketch

```
admin-api
    ├── libs:common, libs:rest-support
    ├── modules:identity, modules:accesscontrol, modules:security-runtime, modules:example-ddd

mobile-api
    ├── libs:common, libs:rest-support
    ├── modules:identity, modules:security-runtime

modules:security-runtime
    └── modules:identity (+ libs:cache, libs:security, …)

modules:accesscontrol
    └── libs:persistence, libs:identity, libs:accesscontrol, libs:audit, libs:cache, libs:security

modules:identity
    └── libs:identity, libs:accesscontrol, libs:audit, libs:cache, libs:security

modules:example-ddd
    └── libs:persistence, libs:identity, libs:security
```

(`libs:identity` depends on `libs:accesscontrol` and `libs:persistence`; diagram omits every transitive edge for brevity.)

## CQRS-lite read side

- `apps:*` own `@BeanParam` / `@QueryParam` binding.
- `libs:identity:query` and `libs:accesscontrol:query` expose pure read-query objects with no JAX-RS dependency.
- Blaze-Persistence and QueryDSL are the current read-side implementations.
- Doma can be added later for reads without changing resource contracts.

## Bounded contexts (RBAC + audit)

Treat `identity` and `accesscontrol` as two packaged slices of one RBAC core. They are separated for ownership and deployment composition, but still share a direct user-role model.

| Bounded Context | Main persistence | Responsibility |
|-----------------|------------------|----------------|
| identity | `SysUser` | Users, authentication orchestration, user–role association |
| accesscontrol | `SysRole`, `SysPermission`, `SysPermissionGroup` | RBAC model and permission catalog |
| audit | `SysOperationLog`, `SysLoginLog` | Operation and login audit |

## Example DDD module

`modules:example-ddd` demonstrates ports/adapters for a small catalog and order flow. Flyway adds tables such as **`ex_product`**, **`ex_order`**, **`ex_order_line`** (see migrator scripts).

Use this module mainly as a **packaging sample**. It is useful for module structure and adapter placement, but it is not positioned as the repository's final statement on rich-domain modeling.

- **`application.command`** — write-side request objects (`CreateExampleProductCommand`, `PlaceExampleOrderCommand`)
- **`application.readmodel`** — projections returned to adapters (`ExampleProductView`, `ExampleOrderView`)
- **`application.port.in`** — inbound ports driving adapters depend on (`ExampleProductCatalogApi`, `ExampleOrderPlacementApi`); application services implement them.
- **`application.port.driven`** — read/write repository boundaries and context helpers (`ExampleCatalogReadRepository`, `ExampleOrderCommandRepository`, `ExampleUserLookupPort`, `ExampleBuyerContext`). (Named **`driven`** rather than `out` to avoid clashing with common `out/` entries in `.gitignore`.)
- **`domain.model`** — domain objects and events (`ExampleOrder`, `ExampleOrderLine`, `ExampleOrderCreatedEvent`)
- **`infrastructure.persistence`** — Panache entities, QueryDSL/Blaze read adapters, and persistence mapping

Admin REST resources inject **port.in** only; Panache/security adapters live under **`infrastructure`** and implement **port.driven**.

The example order flow also writes a record to `app_outbox_message`, giving the repository a
minimal transactional outbox sample for future message dispatch.

## Schema (core, illustrative)

- `sys_user`, `sys_role`, `sys_permission`, `sys_permission_group`
- `sys_user_ref_role`, `sys_role_ref_permission_group`, `sys_permission_group_ref_permission`
- `sys_operation_log`, `sys_login_log`
- Plus example tables above when migrations are applied

## Gradle note

Gradle project `libs:accesscontrol` is exposed as `projects.libs.accesscontrol` (no hyphen in the accessor). **`libs:rest-support`** is `projects.libs.restSupport`.
