# 集成与全链路测试

[English](INTEGRATION_TESTING.md)

## 在 JVM 内模拟「应用已启动」（推荐用于 CI）

模板使用 **`@QuarkusTest`**，配合：

- **PostgreSQL**：Testcontainers JDBC（`jdbc:tc:postgresql:16-alpine:///…`）
- **Valkey**：**`ValkeyTestResource`**（Testcontainers），通过 **`QuarkusPostgresValkeyTestProfile`** 注入 Redis 地址
- **Hibernate `drop-and-create`**：按实体建表，**不必**先跑 **`migrator`**

**全链路流程测试**（不 Mock **`AuthApplicationService`**）：

| 应用 | 类 |
|------|-----|
| admin-api | `com.github.DaiYuANg.integration.AdminApiFullStackFlowIT` |
| mobile-api | `com.github.DaiYuANg.mobile.integration.MobileApiFullStackFlowIT` |

`AdminApiFullStackFlowIT` 当前覆盖：

- `root`（super-admin）登录、JWT 下发、`/me`、`/q/health/live`
- `root` 创建用户、角色、权限组并分配权限
- 使用**同一个 access token** 验证权限扩张与权限回收
- 用户被禁用后，旧 `accessToken` 与旧 `refreshToken` 的失效行为
- 用户改密后，旧 `refreshToken` 失效、旧密码登录失败、新密码登录成功，以及旧 `accessToken` 在过期前仍可继续使用
- 管理端分页 HTTP 契约：只接收零基 `page` / `size`，响应直接返回 `toolkit4j` 的 `PageResult` 字段（`content / page / size / totalElements / totalPages`）
- 管理端权限目录测试种子统一来自 `apps/admin-api/src/test/resources/import-test.sql`

`MobileApiFullStackFlowIT` 现在只保留用户侧进程的基础 smoke test，不再假设存在移动端配置账号。

执行：

```bash
./gradlew :apps:admin-api:test --tests "*FullStackFlowIT*"
./gradlew :apps:mobile-api:test --tests "*FullStackFlowIT*"
```

**环境**：需要本机可运行 Testcontainers（通常即 Docker）。

## 契约测试 vs 全链路

- **`AdminIdentityRestJsonContractTest`** / **`MobileIdentityRestJsonContractTest`**：Mock 应用服务，锁定 **JSON 字段**。
- **`PageQueryCompatibilityTest`** / **`PageResultCompatibilityTest`**：锁定管理端分页契约，避免再次引入 `pageNum/pageSize` 或自定义分页包装字段。
- **`*FullStackFlowIT`**：真实服务、Redis、数据库，更容易发现装配与安全回归。

## 可选：对手动启动的进程做联调

本地起 PostgreSQL + Redis + **`migrator`**，再 **`quarkusDev`**，用 curl/客户端打同样接口。该方式未在仓库里自动化；可重复验证建议仍用上面的 **`@QuarkusTest`** 流程。
