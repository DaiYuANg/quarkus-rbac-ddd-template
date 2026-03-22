# DDD 限界上下文架构说明

[English](ARCHITECTURE_DDD.md)

## 模块结构

```
libs/
├── common              # 共享：Result、异常、PageQuery
├── persistence         # 共享持久化基础：BaseEntity、AuditEntityListener、Repository/Query 基类
├── accesscontrol       # RBAC 上下文：Role、Permission、PermissionGroup
├── identity            # 身份上下文：User（依赖 accesscontrol 的 Role）
├── audit               # 审计上下文：OperationLog、LoginLog
├── redis               # Redis 存储
├── export              # 导出 SPI
└── security            # 认证链、JWT、ActorAuditor

apps/
├── admin-api           # 管理 REST API，依赖 identity、accesscontrol、audit
└── migrator            # Flyway 迁移（独立运行）
```

## admin-api 内部包结构（DDD 风格）

```
com.github.DaiYuANg
├── api/                      # 接口层
│   ├── controller/           # REST 资源（薄控制器）
│   ├── controller.support/   # ExportResponseHelper
│   ├── dto.request/          # Form、Command（UserCreationForm, LoginRequest 等）
│   ├── dto.response/         # VO、Result（UserVO, UserDetailVo, SystemAuthenticationToken）
│   ├── dto.export/           # 导出行（UserExportRow 等）
│   └── handler/              # GlobalExceptionHandler
├── application/              # 应用层（按 bounded context）
│   ├── user/                 # UserApplicationService
│   ├── role/                 # RoleApplicationService
│   ├── permission/           # PermissionApplicationService
│   ├── permissiongroup/      # PermissionGroupApplicationService
│   ├── auth/                 # AuthApplicationService
│   ├── audit/                # OperationLogService, LoginLogService, AuthorityVersionService
│   └── converter/            # ViewMapper, ExportMapper
└── security/                 # Admin 特有的认证/授权适配器
```

## 依赖关系

```
admin-api
    ├── identity (User, UserRepository, UserQueryRepository)
    │       └── accesscontrol (User.roles → SysRole)
    ├── accesscontrol (Role, Permission, PermissionGroup)
    ├── audit (OperationLog, LoginLog)
    └── persistence (BaseEntity, 基类)

identity ──────► accesscontrol
     └─────────► persistence

accesscontrol ─► persistence
audit ──────────► persistence (+ security for AuditSnapshot)
```

## 领域划分

| 限界上下文 | 实体 | 职责 |
|------------|------|------|
| identity | SysUser | 用户、认证、User-Role 关联 |
| accesscontrol | SysRole, SysPermission, SysPermissionGroup | RBAC 角色与权限 |
| audit | SysOperationLog, SysLoginLog | 操作与登录审计 |

## 表结构（未变）

- `sys_user`, `sys_role`, `sys_permission`, `sys_permission_group`
- `sys_user_ref_role`, `sys_role_ref_permission_group`, `sys_permission_group_ref_permission`
- `sys_operation_log`, `sys_login_log`

## Gradle 注意

模块名使用 `accesscontrol`（无连字符）以支持类型安全的 `projects.libs.accesscontrol`。
