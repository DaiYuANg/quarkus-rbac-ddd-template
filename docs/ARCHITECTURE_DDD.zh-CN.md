# 生产架构布局

[English](ARCHITECTURE_DDD.md)

需要看 Mermaid 架构图、收益、演进说明和完整技术选型矩阵时，直接看 [PROJECT_DESIGN.zh-CN.md](PROJECT_DESIGN.zh-CN.md)。

这份布局应该被理解为一个面向生产的 Quarkus modular monolith，并采用了 CQRS-lite 的读写分离思路。REST 资源和 HTTP 查询参数绑定留在各个 `app` 中，领域逻辑、用例和纯 read-query 模型放在 `modules` 与 `libs` 中。

`identity` 和 `accesscontrol` 现在虽然拆成了两个 Gradle 模块，但从业务上看仍然属于同一个 RBAC core，而不是两个完全独立的 bounded context。

## 分层：libs -> modules -> apps

```text
libs/                    # 共享内核：实体、仓储、安全原语、Redis 辅助
modules/                 # 应用服务、端口、按上下文组织的装配
apps/                    # JAX-RS 入口、资源旁 DTO、进程级配置
```

## 仓库目录布局

```text
libs/
|- common                # Result、异常、分页查询对象
|- persistence           # BaseEntity、审计监听器、仓储/查询基类、outbox
|- accesscontrol         # RBAC：角色、权限、权限组
|- identity              # 用户模型
|- audit                 # 操作日志、登录日志
|- cache                 # 刷新令牌、权限版本、登录尝试、目录缓存、nonce
|- security              # 认证链、JWT 辅助、配置用户
`- rest-support          # 共享 JAX-RS：异常映射、RefreshToken Cookie

modules/
|- identity              # 认证应用服务、用户资料、端口
|- accesscontrol         # 用户/角色/权限用例、权限目录加载
|- security-runtime      # Quarkus 安全装配：JWT、权限增强、重放过滤
`- example-ddd           # 示例商品/订单上下文，展示命令、读模型、领域对象与适配器

apps/
|- admin-api             # 管理端 REST 与示例业务资源
|- mobile-api            # C 端认证/session 示例
`- migrator              # Flyway 迁移
```

## admin-api 包布局

REST 资源和请求对象按业务子包组织，不再单独维护一棵 `api/controller` 目录树：

```text
com.github.DaiYuANg.modules
|- identity              # AuthResource、MeResource、LoginAuditEventObserver
|- accesscontrol         # UserResource、RoleResource、Permission*、表单
`- example               # ExampleProductResource、ExampleOrderResource
```

HTTP 横切关注点来自 `libs:rest-support`，例如 `GlobalExceptionHandler` 和 refresh-token cookie 辅助类。

## mobile-api 包布局

`mobile-api` 是第二个独立进程，拥有自己的路由前缀，并通过 `app.identity.*` 区分 JWT 主体类型：

```text
com.github.DaiYuANg.mobile.identity
|- MobileAuthResource
|- MobileMeResource
|- MobileSessionResource
|- MobilePrincipalView
`- LoginAuditEventObserver
```

它只依赖 `modules:identity` 和 `modules:security-runtime`，不承载完整的后台 RBAC CRUD。

## Gradle 依赖草图

```text
admin-api
    |- libs:common, libs:rest-support
    `- modules:identity, modules:accesscontrol, modules:security-runtime, modules:example-ddd

mobile-api
    |- libs:common, libs:rest-support
    `- modules:identity, modules:security-runtime

modules:security-runtime
    `- modules:identity (+ libs:cache, libs:security, ...)

modules:accesscontrol
    `- libs:persistence, libs:identity, libs:accesscontrol, libs:audit, libs:cache, libs:security

modules:identity
    `- libs:identity, libs:accesscontrol, libs:audit, libs:cache, libs:security

modules:example-ddd
    `- libs:persistence, libs:identity, libs:security
```

`libs:identity` 还会继续依赖 `libs:accesscontrol` 和 `libs:persistence`；上图只画关键边。

## CQRS-lite 读侧

- `apps:*` 负责 `@BeanParam` / `@QueryParam` 绑定。
- `libs:identity:query` 与 `libs:accesscontrol:query` 暴露纯读查询对象，不再依赖 JAX-RS。
- 当前读侧实现主要是 Blaze-Persistence 与 QueryDSL。
- Doma 可以作为后续读侧实现引入，不需要修改 REST 契约和应用查询对象。

## 限界上下文

`identity` 与 `accesscontrol` 更准确的描述是同一个 RBAC core 的两个切片。它们为了所有权与部署组合被拆开，但当前仍然共享直接的 user-role 模型。

| 限界上下文 | 主要持久化 | 责任 |
|-----------|-----------|------|
| identity | `SysUser` | 用户、认证编排、用户与角色关联 |
| accesscontrol | `SysRole`、`SysPermission`、`SysPermissionGroup` | RBAC 模型与权限目录 |
| audit | `SysOperationLog`、`SysLoginLog` | 操作审计与登录审计 |

## example-ddd 示例模块

`modules:example-ddd` 用来展示一个小型商品/订单上下文的组织方式。它的定位是“生产模板里的边界参考”，而不是强调 rich domain 的教学样板。

- `application.command`：写侧请求对象，例如 `CreateExampleProductCommand`、`PlaceExampleOrderCommand`
- `application.readmodel`：返回给适配器的读模型，例如 `ExampleProductView`、`ExampleOrderView`
- `application.port.in`：驱动侧依赖的入站端口，例如 `ExampleProductCatalogApi`、`ExampleOrderPlacementApi`
- `application.port.driven`：读写仓储边界与上下文辅助，例如 `ExampleCatalogCommandRepository`、`ExampleCatalogReadRepository`、`ExampleOrderCommandRepository`、`ExampleOrderReadRepository`、`ExampleUserLookupPort`、`ExampleBuyerContext`
- `domain.model`：领域对象与领域事件，例如 `ExampleOrder`、`ExampleOrderLine`、`ExampleOrderCreatedEvent`
- `infrastructure.persistence`：Panache 实体、读侧查询适配器、持久化映射

管理端 REST 资源只依赖 `port.in`。Panache、安全、查询实现这些基础设施适配器都放在 `infrastructure` 下，并实现 `port.driven`。

示例下单流程会在同一事务中向 `app_outbox_message` 写入一条记录，用来展示最小化 transactional outbox 样例，后续可以在此基础上继续补发布器、重试和死信策略。

## 核心表结构

- `sys_user`、`sys_role`、`sys_permission`、`sys_permission_group`
- `sys_user_ref_role`、`sys_role_ref_permission_group`、`sys_permission_group_ref_permission`
- `sys_operation_log`、`sys_login_log`
- 应用迁移后还会增加 `ex_product`、`ex_order`、`ex_order_line`、`app_outbox_message`

## Gradle 说明

Gradle 中的 `libs:accesscontrol` 访问器是 `projects.libs.accesscontrol`，`libs:rest-support` 对应的是 `projects.libs.restSupport`。
