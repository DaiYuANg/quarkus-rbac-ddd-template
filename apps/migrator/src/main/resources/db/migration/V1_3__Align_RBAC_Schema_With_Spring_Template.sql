ALTER TABLE sys_permission
    ADD COLUMN IF NOT EXISTS version INTEGER,
    ADD COLUMN IF NOT EXISTS sort BIGINT,
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE;

ALTER TABLE sys_permission_group
    ADD COLUMN IF NOT EXISTS version INTEGER;

ALTER TABLE sys_role
    ADD COLUMN IF NOT EXISTS version INTEGER,
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE;

ALTER TABLE sys_user
    ADD COLUMN IF NOT EXISTS version INTEGER,
    ADD COLUMN IF NOT EXISTS sort BIGINT,
    ADD COLUMN IF NOT EXISTS avatar BIGINT,
    ADD COLUMN IF NOT EXISTS deleted SMALLINT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS latest_change_password TIMESTAMP WITHOUT TIME ZONE;

CREATE INDEX IF NOT EXISTS idx_sys_permission_sort ON sys_permission (sort);
CREATE INDEX IF NOT EXISTS idx_sys_permission_deleted ON sys_permission (deleted);
CREATE INDEX IF NOT EXISTS idx_sys_role_deleted ON sys_role (deleted);
CREATE INDEX IF NOT EXISTS idx_sys_user_deleted ON sys_user (deleted);
CREATE INDEX IF NOT EXISTS idx_sys_user_latest_sign_in ON sys_user (latest_sign_in DESC);

COMMENT ON COLUMN sys_permission.name IS '资源名称';
COMMENT ON COLUMN sys_permission.resource IS '资源模块';
COMMENT ON COLUMN sys_permission.action IS '权限标识符';
COMMENT ON COLUMN sys_permission.description IS '权限描述';
COMMENT ON COLUMN sys_permission.deleted IS '是否被删除';
COMMENT ON COLUMN sys_permission.sort IS '排序';

COMMENT ON COLUMN sys_permission_group.name IS '权限组名称';
COMMENT ON COLUMN sys_permission_group.code IS '权限组编码';
COMMENT ON COLUMN sys_permission_group.description IS '权限组描述';

COMMENT ON COLUMN sys_role.name IS '角色名称';
COMMENT ON COLUMN sys_role.code IS '角色编码';
COMMENT ON COLUMN sys_role.status IS '角色状态';
COMMENT ON COLUMN sys_role.deleted IS '逻辑删除标识';

COMMENT ON COLUMN sys_user.username IS '用户名';
COMMENT ON COLUMN sys_user.identifier IS '用户唯一标识';
COMMENT ON COLUMN sys_user.mobile_phone IS '手机号';
COMMENT ON COLUMN sys_user.nickname IS '昵称';
COMMENT ON COLUMN sys_user.password IS '密码';
COMMENT ON COLUMN sys_user.avatar IS '用户头像';
COMMENT ON COLUMN sys_user.email IS '用户邮箱';
COMMENT ON COLUMN sys_user.user_status IS '用户状态';
COMMENT ON COLUMN sys_user.deleted IS '逻辑删除标识';
COMMENT ON COLUMN sys_user.latest_sign_in IS '上次登录时间';
COMMENT ON COLUMN sys_user.latest_change_password IS '上次修改密码时间';
