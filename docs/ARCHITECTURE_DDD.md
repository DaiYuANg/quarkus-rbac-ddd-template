# DDD Bounded Context Architecture

[中文](ARCHITECTURE_DDD.zh-CN.md)

## Module Structure

```
libs/
├── common              # Shared: Result, exceptions, PageQuery
├── persistence         # Shared persistence base: BaseEntity, AuditEntityListener, Repository/Query base classes
├── accesscontrol       # RBAC context: Role, Permission, PermissionGroup
├── identity            # Identity context: User (depends on accesscontrol for Role)
├── audit               # Audit context: OperationLog, LoginLog
├── cache               # Infinispan storage
└── security            # Auth chain, JWT, ActorAuditor

apps/
├── admin-api           # Admin REST API, depends on identity, accesscontrol, audit
└── migrator            # Flyway migration (standalone)
```

## admin-api Package Structure (DDD Style)

```
com.github.DaiYuANg
├── api/                      # API layer
│   ├── controller/           # REST resources (thin controllers)
│   ├── dto.request/          # Form, Command (UserCreationForm, LoginRequest, etc.)
│   ├── dto.response/         # VO, Result (UserVO, UserDetailVo, SystemAuthenticationToken)
│   └── handler/              # GlobalExceptionHandler
├── application/              # Application layer (by bounded context)
│   ├── user/                 # UserApplicationService
│   ├── role/                 # RoleApplicationService
│   ├── permission/           # PermissionApplicationService
│   ├── permissiongroup/      # PermissionGroupApplicationService
│   ├── auth/                 # AuthApplicationService
│   ├── audit/                # OperationLogService, LoginLogService, AuthorityVersionService
│   └── converter/            # ViewMapper
└── security/                 # Admin-specific auth/authorization adapters
```

## Dependencies

```
admin-api
    ├── identity (User, UserRepository)
    │       └── accesscontrol (User.roles → SysRole)
    ├── accesscontrol (Role, Permission, PermissionGroup)
    ├── audit (OperationLog, LoginLog)
    └── persistence (BaseEntity, base classes)

identity ──────► accesscontrol
     └─────────► persistence

accesscontrol ─► persistence
audit ──────────► persistence (+ security for AuditSnapshot)
```

## Bounded Contexts

| Bounded Context | Entities | Responsibility |
|-----------------|----------|----------------|
| identity | SysUser | User, authentication, User-Role association |
| accesscontrol | SysRole, SysPermission, SysPermissionGroup | RBAC roles and permissions |
| audit | SysOperationLog, SysLoginLog | Operation and login audit |

## Schema (unchanged)

- `sys_user`, `sys_role`, `sys_permission`, `sys_permission_group`
- `sys_user_ref_role`, `sys_role_ref_permission_group`, `sys_permission_group_ref_permission`
- `sys_operation_log`, `sys_login_log`

## Gradle Note

Module name uses `accesscontrol` (no hyphen) to support type-safe `projects.libs.accesscontrol`.
