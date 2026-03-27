# DDD 限界上下文架构说明

[English](ARCHITECTURE_DDD.md)

请把这份布局理解为**生产型 Quarkus modular monolith + CQRS-lite**：REST 资源和查询参数绑定在 `apps`，纯 read-query 模型在 `libs`/`modules`，而 `identity` 与 `accesscontrol` 目前仍然属于同一个 RBAC core 的两个切片。

**Mermaid 架构图**（系统上下文、Gradle 分层、请求链路）、收益与演进说明、完整技术选型表见 [PROJECT_DESIGN.zh-CN.md](PROJECT_DESIGN.zh-CN.md)。

代码将 **共享内核**（`libs`）、**应用 / 用例模块**（`modules`）与 **可部署适配器**（`apps`）分开。REST 入口在各 `apps` 中；领域逻辑与端口在 `modules` 与 `libs`。

## 分层：libs → modules → apps

```
libs/                    # 共享内核（实体、仓储、安全原语、Redis 辅助）
modules/                 # 各上下文的用例、端口及面向 Quarkus 的装配
apps/                    # JAX-RS 入口、与资源同包的 DTO、进程级配置
```

## 仓库目录结构

```
libs/
├── common              # Result、异常、PageQuery
├── persistence         # BaseEntity、审计监听器、仓储/查询基类
├── accesscontrol       # RBAC：角色、权限、权限组
├── identity            # 用户（依赖 accesscontrol 的角色）
├── audit               # 操作日志、登录日志
├── cache               # 刷新令牌、权限版本、登录尝试、目录缓存、重放 nonce
├── security            # 认证链、JWT 辅助、配置用户
└── rest-support        # 共享 JAX-RS：GlobalExceptionHandler、RefreshTokenCookies

modules/
├── identity            # 认证应用服务、用户资料、端口（基于上述 libs）
├── accesscontrol       # 用户/角色/权限用例、权限目录加载
├── security-runtime    # Quarkus 装配：DB/配置登录、JWT、权限增强、重放过滤
└── example-ddd         # 示例商品/订单限界上下文（端口 + Panache 适配器）

apps/
├── admin-api           # 管理端 REST：com.github.DaiYuANg.modules.*（含示例业务资源）
├── mobile-api          # C 端 REST：com.github.DaiYuANg.mobile.identity.*（认证与 session 示例）
└── migrator            # Flyway（在 validate 等策略下需先于 API 执行）
```

## admin-api 包结构（驱动适配器）

REST 与请求 DTO 按子包放在 `modules` 命名空间下（不再使用单独的 `api/controller` 树）：

```
com.github.DaiYuANg.modules
├── identity              # AuthResource、MeResource、LoginAuditEventObserver
├── accesscontrol         # UserResource、RoleResource、Permission*、表单
└── example               # ExampleProductResource、ExampleOrderResource（example-ddd）
```

统一的 HTTP 横切（`GlobalExceptionHandler`、刷新 Cookie 等）来自 **`libs:rest-support`**，应用依赖该库后由 CDI 注册。

## mobile-api 包结构

独立进程、独立路径前缀，并通过 `app.identity.*` 区分 JWT 主体类型：

```
com.github.DaiYuANg.mobile.identity
├── MobileAuthResource
├── MobileMeResource
├── MobileSessionResource
├── MobilePrincipalView
└── LoginAuditEventObserver
```

依赖 **`modules:identity`** 与 **`modules:security-runtime`**（不包含完整 accesscontrol 管理 CRUD）。

## Gradle 依赖示意

```
admin-api
    ├── libs:common、libs:rest-support
    ├── modules:identity、modules:accesscontrol、modules:security-runtime、modules:example-ddd

mobile-api
    ├── libs:common、libs:rest-support
    ├── modules:identity、modules:security-runtime

modules:security-runtime
    └── modules:identity（及 libs:cache、libs:security 等）

modules:accesscontrol
    └── libs:persistence、libs:identity、libs:accesscontrol、libs:audit、libs:cache、libs:security

modules:identity
    └── libs:identity、libs:accesscontrol、libs:audit、libs:cache、libs:security

modules:example-ddd
    └── libs:persistence、libs:identity、libs:security
```

（`libs:identity` 还依赖 `libs:accesscontrol`、`libs:persistence`；上图为简略，未画出全部传递依赖。）

## 限界上下文（RBAC + 审计）

| 限界上下文 | 主要持久化 | 职责 |
|------------|------------|------|
| identity | `SysUser` | 用户、认证编排、用户–角色关联 |
| accesscontrol | `SysRole`、`SysPermission`、`SysPermissionGroup` | RBAC 模型与权限目录 |
| audit | `SysOperationLog`、`SysLoginLog` | 操作与登录审计 |

## 示例 DDD 模块

`modules:example-ddd` 演示端口/适配器下的目录与下单流程。Flyway 会创建 **`ex_product`**、**`ex_order`**、**`ex_order_line`** 等表（见 migrator 脚本）。

- **`application.port.in`** — 驱动适配器依赖的入站端口（`ExampleProductCatalogApi`、`ExampleOrderPlacementApi`），由应用服务实现。
- **`application.port.driven`** — 由基础设施实现的被驱动侧端口（`ExampleCatalogStore`、`ExampleOrderStore`、`ExampleUserLookupPort`、`ExampleBuyerContext`）。包名用 **`driven`** 而非 `out`，避免与常见 `.gitignore` 中的 `out/` 冲突。

管理端 REST 只注入 **port.in**；Panache/安全适配器放在 **`infrastructure`** 并实现 **port.driven**。

## 表结构（核心，示意）

- `sys_user`、`sys_role`、`sys_permission`、`sys_permission_group`
- `sys_user_ref_role`、`sys_role_ref_permission_group`、`sys_permission_group_ref_permission`
- `sys_operation_log`、`sys_login_log`
- 执行迁移后另有示例表如上

## Gradle 注意

Gradle 工程 `libs:accesscontrol` 在脚本中写作 `projects.libs.accesscontrol`（访问器名无连字符）。**`libs:rest-support`** 对应 `projects.libs.restSupport`。
