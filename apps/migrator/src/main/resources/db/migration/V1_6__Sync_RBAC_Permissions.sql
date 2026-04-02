INSERT INTO sys_permission (id, create_at, update_at, name, code, resource, action, group_code, description, expression)
VALUES
    (1, now(), now(), 'View User', 'user:view', 'user', 'view', 'system.user', 'view users', 'user:view'),
    (2, now(), now(), 'Add User', 'user:add', 'user', 'add', 'system.user', 'add users', 'user:add'),
    (3, now(), now(), 'Edit User', 'user:edit', 'user', 'edit', 'system.user', 'edit users', 'user:edit'),
    (4, now(), now(), 'Delete User', 'user:delete', 'user', 'delete', 'system.user', 'delete users', 'user:delete'),
    (5, now(), now(), 'Assign Role', 'user:assign-role', 'user', 'assign-role', 'system.user', 'assign user roles', 'user:assign-role'),
    (6, now(), now(), 'Reset Password', 'user:reset-password', 'user', 'reset-password', 'system.user', 'reset user passwords', 'user:reset-password'),
    (7, now(), now(), 'View Role', 'role:view', 'role', 'view', 'system.role', 'view roles', 'role:view'),
    (8, now(), now(), 'Add Role', 'role:add', 'role', 'add', 'system.role', 'add roles', 'role:add'),
    (9, now(), now(), 'Edit Role', 'role:edit', 'role', 'edit', 'system.role', 'edit roles', 'role:edit'),
    (10, now(), now(), 'Delete Role', 'role:delete', 'role', 'delete', 'system.role', 'delete roles', 'role:delete'),
    (11, now(), now(), 'Assign Permission Group', 'role:assign-permission-group', 'role', 'assign-permission-group', 'system.role', 'assign permission groups to roles', 'role:assign-permission-group'),
    (12, now(), now(), 'View Permission', 'permission:view', 'permission', 'view', 'system.permission', 'view permissions', 'permission:view'),
    (13, now(), now(), 'Edit Permission', 'permission:edit', 'permission', 'edit', 'system.permission', 'edit permissions', 'permission:edit'),
    (14, now(), now(), 'View Permission Group', 'permission-group:view', 'permission-group', 'view', 'system.permission-group', 'view permission groups', 'permission-group:view'),
    (15, now(), now(), 'Add Permission Group', 'permission-group:add', 'permission-group', 'add', 'system.permission-group', 'add permission groups', 'permission-group:add'),
    (16, now(), now(), 'Edit Permission Group', 'permission-group:edit', 'permission-group', 'edit', 'system.permission-group', 'edit permission groups', 'permission-group:edit'),
    (17, now(), now(), 'Delete Permission Group', 'permission-group:delete', 'permission-group', 'delete', 'system.permission-group', 'delete permission groups', 'permission-group:delete'),
    (18, now(), now(), 'Assign Permission', 'permission-group:assign-permission', 'permission-group', 'assign-permission', 'system.permission-group', 'assign permissions to groups', 'permission-group:assign-permission'),
    (19, now(), now(), 'Change Password', 'auth:change-password', 'auth', 'change-password', 'system.auth', 'change own password', 'auth:change-password')
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    resource = EXCLUDED.resource,
    action = EXCLUDED.action,
    group_code = EXCLUDED.group_code,
    description = EXCLUDED.description,
    expression = EXCLUDED.expression,
    update_at = now();

INSERT INTO sys_permission_group_ref_permission (permission_group_id, permission_id)
SELECT 1, p.id
FROM sys_permission p
WHERE p.code IN (
    'user:view',
    'user:add',
    'user:edit',
    'user:delete',
    'user:assign-role',
    'user:reset-password',
    'role:view',
    'role:add',
    'role:edit',
    'role:delete',
    'role:assign-permission-group',
    'permission:view',
    'permission:edit',
    'permission-group:view',
    'permission-group:add',
    'permission-group:edit',
    'permission-group:delete',
    'permission-group:assign-permission',
    'auth:change-password'
)
ON CONFLICT DO NOTHING;
