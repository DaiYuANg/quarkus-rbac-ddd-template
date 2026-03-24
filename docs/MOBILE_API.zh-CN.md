# 移动端进程（`apps:mobile-api`）

[English](MOBILE_API.md)

移动端（C 端）Quarkus 应用与 `admin-api` 是**不同部署单元**。它复用 **identity** 与 **security-runtime**，但**刻意不依赖** RBAC 管理类 module，避免 C 端 HTTP 误连用户/角色/权限等后台能力。

## 依赖白名单（Gradle）

`apps/mobile-api/build.gradle.kts` 中**预期**的 `implementation` 依赖如下：

| 依赖 | 作用 |
|------|------|
| `libs:common` | `Result`、共享异常、分页等 |
| `libs:rest-support` | 统一异常映射、刷新令牌 Cookie 工具 |
| `modules:identity` | 登录、资料、`AuthApplicationService`、DTO |
| `modules:security-runtime` | JWT 管线、Quarkus 安全装配、登录编排钩子 |
| Quarkus 各 bundle | REST、观测、安全等（随上述模块传递引入 Hibernate 等） |

**未经架构评审请勿添加**：

- `modules:accesscontrol` — 会把 RBAC 管理服务放进 mobile 的 classpath。
- `modules:example-ddd` — 示例业务域；模板中默认仅 admin 暴露，除非你要做公开目录类 API。

若要为 mobile 增加能力：

1. 优先扩展 **`modules:identity`**（新用例/端口），或新增**独立限界上下文 module**，且不依赖后台 CRUD。
2. 仅在模块对外 API 适合 C 端威胁模型时，再在 `mobile-api` 增加 **`implementation(projects.modules.yourContext)`**。

## 配置差异

- **`app.identity.db-user-type` / `config-user-fallback-type`** — JWT 中 `userType` 与 admin 区分（模板中 `MEMBER` vs `ADMIN`）。
- **HTTP 端口** — 默认 `8081`，admin 为 `8080`。
- **Cookie 路径** — `/api/mobile/v1/auth`，与 admin 的 `/api/v1/auth` 隔离，避免刷新 Cookie 串进程。

## 契约测试

登录与 `/me` 的 JSON 形态由 `MobileIdentityRestJsonContractTest`（REST Assured）锁定。重构时请保持字段名与 `Result` 信封稳定，或做 API 版本化。
