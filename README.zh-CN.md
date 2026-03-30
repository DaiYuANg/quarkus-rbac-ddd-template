# Quarkus RBAC 模板

[English](README.md)

可复用的 Quarkus 后端基础模板，提供 RBAC（基于角色的访问控制）、JWT 认证与 DDD 风格架构。

## 特性

- **安全**：Quarkus Security、JWT、刷新令牌、登录提供者链
- **RBAC**：用户、角色、权限、权限组，基于 Valkey 缓存的权限快照
- **持久化**：Hibernate ORM + Panache，Blaze-Persistence + Entity View 查询
- **DDD**：限界上下文 **modules**（`identity`、`accesscontrol`、`security-runtime`、示例 `example-ddd`）与共享 **libs**
- **应用**：**admin-api**（管理端 REST）、**mobile-api**（C 端示例进程，独立路由与 `app.identity.*` 主体类型）
- **横切**：**libs:rest-support**（统一异常映射、刷新 Cookie）、可选 **@ReplayProtected**（时间戳 + nonce，Redis）
- **代码质量**：Checkstyle、SpotBugs、JaCoCo、OWASP、Spotless；编译开启 **`-Xlint:deprecation`**

## 快速开始

### 环境要求

- **JDK 25**（Gradle 工具链；代码使用 JDK 25 语言特性，例如灵活构造器体）
- PostgreSQL
- Valkey/Redis（开发可选，生产必需；使用 docker-compose 或本地安装）

**本地配置**：将 `gradle.properties.template` 复制为 `gradle.properties`，按本机环境调整（JVM 参数、代理等）。`gradle.properties` 已加入 gitignore。

**JWT 密钥**：首次启动前执行 `./gradlew generateRsaKeys`，密钥生成到项目根目录并已加入 gitignore。

**默认 super-admin**（不走数据库）：`root` / `root` — 来自 `app.security.super-admin`，用于管理端快速测试接口。

### 执行数据库迁移

在 **`validate`** 等策略下，需**先**跑迁移再启动 API：

```bash
./gradlew :apps:migrator:quarkusRun
```

### 启动管理 API

```bash
./gradlew :apps:admin-api:quarkusDev
```

默认 HTTP：`http://localhost:8080`（见 `apps/admin-api/src/main/resources/application.yaml`）。

### 启动移动端 API（示例进程）

```bash
./gradlew :apps:mobile-api:quarkusDev
```

默认 HTTP：`http://localhost:8081`。与 admin 共用同一数据库与 Redis（除非改配置）。当前不再内置移动端配置账号。

## 项目结构

```
libs/
├── common              # Result、异常、PageQuery
├── persistence         # BaseEntity、审计监听器、基础仓储、查询基类
├── accesscontrol       # 角色、权限、权限组（RBAC）
├── identity            # 用户（依赖 accesscontrol）
├── audit               # 操作日志、登录日志
├── cache               # 刷新令牌、权限版本、登录尝试、权限目录、重放 nonce（Redis）
├── security            # 认证链、JWT、当前用户、主体定义、配置
└── rest-support        # 共享 GlobalExceptionHandler、RefreshTokenCookies（JAX-RS）

modules/
├── identity            # 认证应用服务、用户资料、端口
├── accesscontrol       # 用户/角色/权限应用服务、权限目录加载
├── security-runtime    # Quarkus 装配：DB/super-admin 登录、JWT、权限增强、重放过滤
└── example-ddd         # 示例商品/订单限界上下文（端口 + Panache 适配器）

apps/
├── admin-api           # 管理端 REST（identity、accesscontrol、示例业务接口）
├── mobile-api          # C 端 REST（认证与 session 示例；无管理端 CRUD）
└── migrator            # Flyway 迁移（一次性执行）
```

## 配置

- **dev**：各应用 `application.yaml` 中的本地 PostgreSQL / Redis 默认项
- **test**：Testcontainers PostgreSQL；部分 profile 启用 Redis dev services
- **prod**：数据源与 Redis 来自环境变量（示例见 `docker-compose-prod.yaml`）

常用配置项：

- **`app.identity.*`**：数据库用户写入 JWT / 快照时的 `userType`（admin 与 mobile 可不同）
- **`app.replay.*`**：对标注 `@ReplayProtected` 的接口启用防重放（admin 示例接口）

## 文档

| 文档 | English | 中文 |
|------|---------|------|
| 项目设计（DDD、架构图、技术栈） | [PROJECT_DESIGN.md](docs/PROJECT_DESIGN.md) | [PROJECT_DESIGN.zh-CN.md](docs/PROJECT_DESIGN.zh-CN.md) |
| Security 运行时（认证链、快照流、Mermaid 图） | [SECURITY_RUNTIME.md](docs/SECURITY_RUNTIME.md) | [SECURITY_RUNTIME.zh-CN.md](docs/SECURITY_RUNTIME.zh-CN.md) |
| Mobile API（依赖白名单） | [MOBILE_API.md](docs/MOBILE_API.md) | [MOBILE_API.zh-CN.md](docs/MOBILE_API.zh-CN.md) |
| DDD 架构说明 | [ARCHITECTURE_DDD.md](docs/ARCHITECTURE_DDD.md) | [ARCHITECTURE_DDD.zh-CN.md](docs/ARCHITECTURE_DDD.zh-CN.md) |
| 授权流程 | [AUTHORIZATION_FLOW.md](docs/AUTHORIZATION_FLOW.md) | [AUTHORIZATION_FLOW.zh-CN.md](docs/AUTHORIZATION_FLOW.zh-CN.md) |
| 生产就绪清单 | [PRODUCTION_READINESS_CHECKLIST.md](docs/PRODUCTION_READINESS_CHECKLIST.md) | [PRODUCTION_READINESS_CHECKLIST.zh-CN.md](docs/PRODUCTION_READINESS_CHECKLIST.zh-CN.md) |
| 故障排查 | [TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) | [TROUBLESHOOTING.zh-CN.md](docs/TROUBLESHOOTING.zh-CN.md) |

索引：[docs/README.md](docs/README.md)

## 构建任务

### 代码质量

```bash
./gradlew check                    # Checkstyle、SpotBugs、测试
./gradlew spotlessApply            # 格式化代码
./gradlew dependencyCheckAnalyze   # OWASP 漏洞扫描
```

### buildSrc 任务

| 任务 | 说明 |
|------|------|
| `replacePackage` | 批量替换包名与 group。`-PfromPackage=... -PtoPackage=...` |
| `generateRsaKeys` | 生成 JWT RSA 密钥（项目根目录）。首次启动前需执行。可选 `-PoutputDir=...` |

详见 [buildSrc/README.md](buildSrc/README.md)。

## License

见仓库许可信息。
