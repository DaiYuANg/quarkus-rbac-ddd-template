# Production Readiness Checklist

[中文](PRODUCTION_READINESS_CHECKLIST.zh-CN.md)

## Security
- Verify JWT signing keys are injected from secret storage, not committed defaults.
- Confirm refresh token TTL, revoke policy, and rotation strategy.
- Confirm `PermissionSnapshotStore` Valkey/Redis key prefix/namespace isolation per environment.
- Validate authorization denial logs are enabled and retained.

## Authorization
- Login must publish permission snapshots to Valkey/Redis.
- `SecurityIdentityAugmentor` must reload snapshots when `authorityVersion` changes.
- Service-layer authorization must protect role binding, permission-group binding, password reset, and similar sensitive operations.
- Static endpoint guards should prefer Quarkus annotations, with `@PermissionChecker` for identity-aware exceptions such as self-service flows.

## Database
- Run migrator before starting `admin-api`.
- Ensure Flyway history table and baseline strategy match target environment.
- Verify `sys_permission` unique constraints for `code` and `(resource, action)`.

## Observability
- Health, info, metrics, and OTEL endpoints should be exposed only on the management interface.
- Confirm Prometheus scrape path and OTLP exporter endpoint per environment.

## Runtime validation
- Test login, refresh, logout, permission change, and post-change access denial.
- Test Valkey/Redis outage behavior for login and authorization snapshot fallback.
- Test denied authorization audit records.
