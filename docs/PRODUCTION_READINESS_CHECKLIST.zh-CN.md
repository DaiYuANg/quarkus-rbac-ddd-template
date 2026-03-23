# 生产就绪清单

[English](PRODUCTION_READINESS_CHECKLIST.md)

## 安全

- 确认 JWT 签名密钥来自密钥存储注入，而非提交的默认值。
- 确认刷新令牌 TTL、吊销策略和轮换策略。
- 确认 `PermissionSnapshotStore` 在各环境中的 Infinispan 缓存/命名空间隔离。
- 验证授权拒绝日志已启用并保留。

## 授权

- 登录必须将权限快照发布到 Infinispan。
- `SecurityIdentityAugmentor` 必须在 `authorityVersion` 变更时重新加载快照。
- 服务层授权必须保护角色绑定、权限组绑定、密码重置等敏感操作。
- 端点静态守卫可继续使用 Quarkus 注解，复杂检查须通过 `AuthorizationService`。

## 数据库

- 启动 `admin-api` 前先运行 migrator。
- 确保 Flyway 历史表与 baseline 策略与目标环境一致。
- 验证 `sys_permission` 对 `code` 和 `(domain, resource, action)` 的唯一约束。

## 可观测性

- Health、info、metrics、OTEL 端点应仅在管理接口暴露。
- 确认各环境的 Prometheus scrape 路径和 OTLP exporter 端点。

## 运行时验证

- 测试登录、刷新、登出、权限变更及变更后的访问拒绝。
- 测试 Infinispan 故障时登录与授权快照回退行为。
- 测试授权拒绝审计记录。
