INSERT INTO sys_permission (id, create_at, update_at, name, code, domain, resource, action, group_code, description, expression)
VALUES
    (1, now(), now(), '用户查看', 'system.user:view', 'system', 'user', 'view', 'system.user', 'view users', 'system.user:view'),
    (2, now(), now(), '用户新增', 'system.user:add', 'system', 'user', 'add', 'system.user', 'add users', 'system.user:add'),
    (3, now(), now(), '用户编辑', 'system.user:edit', 'system', 'user', 'edit', 'system.user', 'edit users', 'system.user:edit'),
    (4, now(), now(), '用户删除', 'system.user:delete', 'system', 'user', 'delete', 'system.user', 'delete users', 'system.user:delete'),
    (5, now(), now(), '角色查看', 'system.role:view', 'system', 'role', 'view', 'system.role', 'view roles', 'system.role:view'),
    (6, now(), now(), '角色新增', 'system.role:add', 'system', 'role', 'add', 'system.role', 'add roles', 'system.role:add'),
    (7, now(), now(), '角色编辑', 'system.role:edit', 'system', 'role', 'edit', 'system.role', 'edit roles', 'system.role:edit'),
    (8, now(), now(), '角色删除', 'system.role:delete', 'system', 'role', 'delete', 'system.role', 'delete roles', 'system.role:delete'),
    (9, now(), now(), '权限查看', 'system.permission:view', 'system', 'permission', 'view', 'system.permission', 'view permissions', 'system.permission:view'),
    (10, now(), now(), '权限组查看', 'system.permission-group:view', 'system', 'permission-group', 'view', 'system.permission-group', 'view permission groups', 'system.permission-group:view'),
    (11, now(), now(), '权限组新增', 'system.permission-group:add', 'system', 'permission-group', 'add', 'system.permission-group', 'add permission groups', 'system.permission-group:add'),
    (12, now(), now(), '权限组编辑', 'system.permission-group:edit', 'system', 'permission-group', 'edit', 'system.permission-group', 'edit permission groups', 'system.permission-group:edit'),
    (13, now(), now(), '权限组删除', 'system.permission-group:delete', 'system', 'permission-group', 'delete', 'system.permission-group', 'delete permission groups', 'system.permission-group:delete')
ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_permission_group (id, create_at, update_at, name, description, code, sort)
VALUES (1, now(), now(), '系统管理', 'system management', 'system-admin', 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_role (id, create_at, update_at, name, code, status, sort)
VALUES (1, now(), now(), '超级管理员', 'super-admin', 'ENABLED', 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_user (id, create_at, update_at, username, password, identifier, mobile_phone, nickname, email, latest_sign_in, user_status)
VALUES (1, now(), now(), 'root', '$2a$10$luMSnAos9B8gjWQj6YvzjueaTY6fmV6S0x2drXr/7EQo1leChX2GC', 'root-identifier', '13800000000', 'root', 'root@example.com', now(), 'ENABLED')
ON CONFLICT (id) DO NOTHING;

INSERT INTO sys_permission_group_ref_permission (permission_group_id, permission_id)
VALUES
    (1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,10),(1,11),(1,12),(1,13)
ON CONFLICT DO NOTHING;

INSERT INTO sys_role_ref_permission_group (role_id, permission_group_id)
VALUES (1,1)
ON CONFLICT DO NOTHING;

INSERT INTO sys_user_ref_role (user_id, role_id)
VALUES (1,1)
ON CONFLICT DO NOTHING;

SELECT setval('sys_permission_id_seq', GREATEST(COALESCE((SELECT MAX(id) FROM sys_permission), 1), 1), true);
SELECT setval('sys_permission_group_id_seq', GREATEST(COALESCE((SELECT MAX(id) FROM sys_permission_group), 1), 1), true);
SELECT setval('sys_role_id_seq', GREATEST(COALESCE((SELECT MAX(id) FROM sys_role), 1), 1), true);
SELECT setval('sys_user_id_seq', GREATEST(COALESCE((SELECT MAX(id) FROM sys_user), 1), 1), true);
