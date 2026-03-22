# Quarkus RBAC 模板

[English](README.md)

可复用的 Quarkus 后端基础模板，提供 RBAC（基于角色的访问控制）、JWT 认证与 DDD 风格架构。

## 特性

- **安全**：Quarkus Security、JWT、刷新令牌、登录提供者链
- **RBAC**：用户、角色、权限、权限组，基于 Redis 缓存的权限快照
- **持久化**：Hibernate ORM + Panache，Blaze-Persistence + Entity View 查询
- **DDD**：按限界上下文划分模块（identity、accesscontrol、audit）
- **代码质量**：Checkstyle、SpotBugs、JaCoCo、OWASP 依赖检查、Spotless

## 快速开始

### 环境要求

- JDK 25+
- PostgreSQL
- Redis（开发可选，生产必需）

### 执行数据库迁移

```bash
./gradlew :apps:migrator:quarkusRun
```

### 启动管理 API

```bash
./gradlew :apps:admin-api:quarkusDev
```

## 项目结构

```
libs/
├── common              # Result、异常、PageQuery
├── persistence         # BaseEntity、审计监听器、基础仓储、查询基类
├── accesscontrol       # 角色、权限、权限组（RBAC）
├── identity            # 用户（依赖 accesscontrol）
├── audit               # 操作日志、登录日志
├── redis               # 刷新令牌、权限版本、登录尝试状态
├── export              # 导出 SPI
└── security            # 认证提供者链、JWT、当前用户、令牌上下文

apps/
├── admin-api           # 管理 REST API
└── migrator            # Flyway 迁移（一次性执行）
```

## 配置

- **dev**：本地 PostgreSQL 默认配置
- **test**：`rbac_test` 数据库
- **prod**：从环境变量读取 `DB_JDBC_URL`、`DB_USERNAME`、`DB_PASSWORD`

## 文档

| 文档 | English | 中文 |
|------|---------|------|
| DDD 架构说明 | [ARCHITECTURE_DDD.md](docs/ARCHITECTURE_DDD.md) | [ARCHITECTURE_DDD.zh-CN.md](docs/ARCHITECTURE_DDD.zh-CN.md) |
| 授权流程 | [AUTHORIZATION_FLOW.md](docs/AUTHORIZATION_FLOW.md) | [AUTHORIZATION_FLOW.zh-CN.md](docs/AUTHORIZATION_FLOW.zh-CN.md) |
| 生产就绪清单 | [PRODUCTION_READINESS_CHECKLIST.md](docs/PRODUCTION_READINESS_CHECKLIST.md) | [PRODUCTION_READINESS_CHECKLIST.zh-CN.md](docs/PRODUCTION_READINESS_CHECKLIST.zh-CN.md) |

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
| `generateRsaKeys` | 生成 RSA 密钥对（privateKey.pem、publicKey.pem）。可选 `-PoutputDir=...` |

详见 [buildSrc/README.md](buildSrc/README.md)。

## License

见仓库许可信息。
