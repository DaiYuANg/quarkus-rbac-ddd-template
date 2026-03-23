# 故障排查

## Valkey/Redis：连接被拒绝

应用无法连接 Valkey/Redis 时：

1. 确认 Valkey 或 Redis 已启动：在项目根目录执行 `docker compose up -d valkey`（或 `redis`）。
2. 检查 `application.yaml` 中的 `quarkus.redis.hosts` 与当前环境一致（默认：`redis://localhost:6379`）。
3. 生产环境需设置环境变量 `QUARKUS_REDIS_HOSTS`（例如 Docker 中：`redis://valkey:6379`）。

## 数据库连接问题

若 PostgreSQL 连接失败，请检查数据源 URL、用户名和密码。使用 Docker 时，确保在启动应用前数据库容器已就绪。
