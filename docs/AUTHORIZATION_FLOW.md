# Authorization Flow

[中文](AUTHORIZATION_FLOW.zh-CN.md)

## Request-time flow
1. Request enters Quarkus Security.
2. Existing JWT is parsed and turned into a `SecurityIdentity`.
3. `AdminPermissionSecurityIdentityAugmentor` tries Redis permission snapshot first.
4. If snapshot is absent or authority version mismatches, it reloads through `PermissionSnapshotLoader`.
5. Effective permissions are attached back onto `SecurityIdentity`.
6. Endpoint-level static protection can still use Quarkus annotations.
7. Service-level complex protection must call `AuthorizationService`.

## Snapshot lifecycle
- Login success publishes a permission snapshot to Redis.
- Refresh success republishes a permission snapshot.
- Role / permission / permission-group changes must bump authority version.
- Authority version mismatch forces snapshot refresh from database.

## Recommended usage
- Use `@PermissionsAllowed` for fixed endpoint entry permissions.
- Use `AuthorizationService.check(...)` for business-sensitive operations.
- Use `checkAny(...)` / `checkAll(...)` where one action may be granted by multiple permission codes.
