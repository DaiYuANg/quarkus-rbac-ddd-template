# 授权流程

[English](AUTHORIZATION_FLOW.md)

更完整的认证链路与 Redis / PostgreSQL 数据流见 [SECURITY_RUNTIME.zh-CN.md](SECURITY_RUNTIME.zh-CN.md)。

## 请求时流程

1. 请求进入 Quarkus Security。
2. 已有 JWT 被解析并转换为 `SecurityIdentity`。
3. `AdminPermissionSecurityIdentityAugmentor` 优先尝试 Valkey/Redis 权限快照。
4. 若快照缺失、绑定信息不一致或权限版本不一致，通过 `PermissionSnapshotLoader` 重新加载。
5. 有效权限附加回 `SecurityIdentity`。
6. 端点级静态保护仍可使用 Quarkus 注解。
7. 授权主路径以 `@PermissionsAllowed` 和 `@PermissionChecker` 为主；只有少数无法在资源层表达的业务保护才保留在 service 内部。

## 快照生命周期

- 登录成功将权限快照发布到 Valkey/Redis。
- 刷新成功重新发布权限快照。
- `super-admin` 的权限来自权限目录全量代码，不是单独维护的静态权限清单。
- 角色 / 权限 / 权限组变更需递增 authority version。
- Authority version 不一致会强制从数据库刷新快照。

## 推荐用法

- 固定端点入口权限使用 `@PermissionsAllowed`。
- 带“本人可操作自己”这类条件时使用 `@PermissionChecker`。
- 只有当权限规则依赖方法内部业务状态、且不适合放在资源层时，才在 service 中保留少量授权分支。
