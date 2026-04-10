-- Keep RBAC seed data idempotent against both fresh databases and baseline-on-migrate
-- environments that may already contain a pre-Flyway permission catalog.
CREATE TEMP TABLE tmp_seed_sys_permission
(
    name        VARCHAR(128) NOT NULL,
    code        VARCHAR(128) NOT NULL,
    resource    VARCHAR(128) NOT NULL,
    action      VARCHAR(128) NOT NULL,
    group_code  VARCHAR(128),
    description VARCHAR(255),
    expression  VARCHAR(255),
    PRIMARY KEY (code)
) ON COMMIT DROP;

INSERT INTO tmp_seed_sys_permission (name, code, resource, action, group_code, description, expression)
VALUES
    ('View User', 'user:view', 'user', 'view', 'system.user', 'view users', 'user:view'),
    ('Add User', 'user:add', 'user', 'add', 'system.user', 'add users', 'user:add'),
    ('Edit User', 'user:edit', 'user', 'edit', 'system.user', 'edit users', 'user:edit'),
    ('Delete User', 'user:delete', 'user', 'delete', 'system.user', 'delete users', 'user:delete'),
    ('Assign Role', 'user:assign-role', 'user', 'assign-role', 'system.user', 'assign user roles', 'user:assign-role'),
    ('Reset Password', 'user:reset-password', 'user', 'reset-password', 'system.user', 'reset user passwords', 'user:reset-password'),
    ('View Role', 'role:view', 'role', 'view', 'system.role', 'view roles', 'role:view'),
    ('Add Role', 'role:add', 'role', 'add', 'system.role', 'add roles', 'role:add'),
    ('Edit Role', 'role:edit', 'role', 'edit', 'system.role', 'edit roles', 'role:edit'),
    ('Delete Role', 'role:delete', 'role', 'delete', 'system.role', 'delete roles', 'role:delete'),
    ('Assign Permission Group', 'role:assign-permission-group', 'role', 'assign-permission-group', 'system.role', 'assign permission groups to roles', 'role:assign-permission-group'),
    ('View Permission', 'permission:view', 'permission', 'view', 'system.permission', 'view permissions', 'permission:view'),
    ('Edit Permission', 'permission:edit', 'permission', 'edit', 'system.permission', 'edit permissions', 'permission:edit'),
    ('View Permission Group', 'permission-group:view', 'permission-group', 'view', 'system.permission-group', 'view permission groups', 'permission-group:view'),
    ('Add Permission Group', 'permission-group:add', 'permission-group', 'add', 'system.permission-group', 'add permission groups', 'permission-group:add'),
    ('Edit Permission Group', 'permission-group:edit', 'permission-group', 'edit', 'system.permission-group', 'edit permission groups', 'permission-group:edit'),
    ('Delete Permission Group', 'permission-group:delete', 'permission-group', 'delete', 'system.permission-group', 'delete permission groups', 'permission-group:delete'),
    ('Assign Permission', 'permission-group:assign-permission', 'permission-group', 'assign-permission', 'system.permission-group', 'assign permissions to groups', 'permission-group:assign-permission'),
    ('Change Password', 'auth:change-password', 'auth', 'change-password', 'system.auth', 'change own password', 'auth:change-password');

-- 1) Sync rows that already use the canonical code.
UPDATE sys_permission p
SET name = s.name,
    resource = s.resource,
    action = s.action,
    group_code = s.group_code,
    description = s.description,
    expression = s.expression,
    update_at = now()
FROM tmp_seed_sys_permission s
WHERE p.code = s.code;

-- 2) Normalize legacy rows keyed by the unique (resource, action) pair.
UPDATE sys_permission p
SET name = s.name,
    code = s.code,
    group_code = s.group_code,
    description = s.description,
    expression = s.expression,
    update_at = now()
FROM tmp_seed_sys_permission s
WHERE p.resource = s.resource
  AND p.action = s.action
  AND p.code <> s.code
  AND NOT EXISTS
    (
        SELECT 1
        FROM sys_permission existing
        WHERE existing.code = s.code
          AND existing.id <> p.id
    );

-- 3) Last-resort normalization for pre-existing rows that only match by unique name.
UPDATE sys_permission p
SET code = s.code,
    resource = s.resource,
    action = s.action,
    group_code = s.group_code,
    description = s.description,
    expression = s.expression,
    update_at = now()
FROM tmp_seed_sys_permission s
WHERE p.name = s.name
  AND
    (
        p.code <> s.code
        OR p.resource <> s.resource
        OR p.action <> s.action
        OR COALESCE(p.group_code, '') <> COALESCE(s.group_code, '')
        OR COALESCE(p.description, '') <> COALESCE(s.description, '')
        OR COALESCE(p.expression, '') <> COALESCE(s.expression, '')
    )
  AND NOT EXISTS
    (
        SELECT 1
        FROM sys_permission existing
        WHERE existing.code = s.code
          AND existing.id <> p.id
    )
  AND NOT EXISTS
    (
        SELECT 1
        FROM sys_permission existing
        WHERE existing.resource = s.resource
          AND existing.action = s.action
          AND existing.id <> p.id
    );

-- 4) Insert any missing canonical permissions without pinning database ids.
INSERT INTO sys_permission
    (create_at, update_at, name, code, resource, action, group_code, description, expression)
SELECT now(),
       now(),
       s.name,
       s.code,
       s.resource,
       s.action,
       s.group_code,
       s.description,
       s.expression
FROM tmp_seed_sys_permission s
WHERE NOT EXISTS
    (
        SELECT 1
        FROM sys_permission p
        WHERE p.code = s.code
           OR (p.resource = s.resource AND p.action = s.action)
           OR p.name = s.name
    )
ON CONFLICT DO NOTHING;
