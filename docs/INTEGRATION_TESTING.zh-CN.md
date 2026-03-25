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

覆盖 **登录 → JWT → `/me`** 以及 **存活探针**（配置用户 `root` / `mobile-member`），与本地 **`quarkusDev` + 配置用户** 接近。

**刷新令牌** 未包含：`AdminRefreshTokenAuthenticationProvider` 目前只从 **`UserRepository`** 加载用户，纯配置用户无法走通刷新。若需测刷新，请另写 IT 并插入 `sys_user` 等种子数据。

执行：

```bash
./gradlew :apps:admin-api:test --tests "*FullStackFlowIT*"
./gradlew :apps:mobile-api:test --tests "*FullStackFlowIT*"
```

**环境**：需要本机可运行 Testcontainers（通常即 Docker）。

## 契约测试 vs 全链路

- **`AdminIdentityRestJsonContractTest`** / **`MobileIdentityRestJsonContractTest`**：Mock 应用服务，锁定 **JSON 字段**。
- **`*FullStackFlowIT`**：真实服务、Redis、数据库，更容易发现装配与安全回归。

## 可选：对手动启动的进程做联调

本地起 PostgreSQL + Redis + **`migrator`**，再 **`quarkusDev`**，用 curl/客户端打同样接口。该方式未在仓库里自动化；可重复验证建议仍用上面的 **`@QuarkusTest`** 流程。
