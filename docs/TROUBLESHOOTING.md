# Troubleshooting

## Valkey/Redis: Connection refused

When the application fails to connect to Valkey/Redis:

1. Ensure Valkey or Redis is running: `docker compose up -d valkey` (or `redis`) from the project root.
2. Check `quarkus.redis.hosts` in `application.yaml` matches your setup (default: `redis://localhost:6379`).
3. For production, set `QUARKUS_REDIS_HOSTS` environment variable (e.g. `redis://valkey:6379` in Docker).

## Database connection issues

If PostgreSQL connection fails, verify the datasource URL, username, and password. For Docker, ensure the database container is healthy before starting the application.
