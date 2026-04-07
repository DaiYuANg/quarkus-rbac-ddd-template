-- RBAC schema (merged from init + align + bootstrap)
CREATE TABLE IF NOT EXISTS sys_permission
(
    id          BIGSERIAL PRIMARY KEY,
    create_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    update_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    create_by   VARCHAR(128),
    update_by   VARCHAR(128),
    name        VARCHAR(128)                NOT NULL,
    code        VARCHAR(128)                NOT NULL,
    resource    VARCHAR(128)                NOT NULL,
    action      VARCHAR(128)                NOT NULL,
    group_code  VARCHAR(128),
    description VARCHAR(255),
    expression  VARCHAR(255),
    version     INTEGER,
    sort        BIGINT,
    deleted     BOOLEAN                     DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_permission_name ON sys_permission (name);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_permission_code ON sys_permission (code);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_permission_resource_action ON sys_permission (resource, action);
CREATE INDEX IF NOT EXISTS idx_sys_permission_resource ON sys_permission (resource);
CREATE INDEX IF NOT EXISTS idx_sys_permission_action ON sys_permission (action);
CREATE INDEX IF NOT EXISTS idx_sys_permission_group_code ON sys_permission (group_code);
CREATE INDEX IF NOT EXISTS idx_sys_permission_sort ON sys_permission (sort);
CREATE INDEX IF NOT EXISTS idx_sys_permission_deleted ON sys_permission (deleted);

CREATE TABLE IF NOT EXISTS sys_permission_group
(
    id          BIGSERIAL PRIMARY KEY,
    create_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    update_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    create_by   VARCHAR(128),
    update_by   VARCHAR(128),
    name        VARCHAR(128)                NOT NULL,
    description VARCHAR(255),
    code        VARCHAR(128)                NOT NULL,
    sort        INTEGER                     NOT NULL DEFAULT 0,
    version     INTEGER
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_permission_group_name ON sys_permission_group (name);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_permission_group_code ON sys_permission_group (code);
CREATE INDEX IF NOT EXISTS idx_sys_permission_group_sort ON sys_permission_group (sort);

CREATE TABLE IF NOT EXISTS sys_role
(
    id          BIGSERIAL PRIMARY KEY,
    create_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    update_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    description VARCHAR(128),
    create_by   VARCHAR(128),
    update_by   VARCHAR(128),
    name        VARCHAR(128)                NOT NULL,
    code        VARCHAR(128)                NOT NULL,
    status      SMALLINT                    NOT NULL,
    sort        INTEGER                     NOT NULL DEFAULT 0,
    version     INTEGER,
    deleted     BOOLEAN                     DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_name ON sys_role (name);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_code ON sys_role (code);
CREATE INDEX IF NOT EXISTS idx_sys_role_status ON sys_role (status);
CREATE INDEX IF NOT EXISTS idx_sys_role_sort ON sys_role (sort);
CREATE INDEX IF NOT EXISTS idx_sys_role_deleted ON sys_role (deleted);

CREATE TABLE IF NOT EXISTS sys_user
(
    id                     BIGSERIAL PRIMARY KEY,
    create_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    update_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    create_by              VARCHAR(128),
    update_by              VARCHAR(128),
    username               VARCHAR(128)                NOT NULL,
    password               VARCHAR(255)                NOT NULL,
    identifier             VARCHAR(128)                NOT NULL,
    mobile_phone           VARCHAR(64),
    nickname               VARCHAR(128),
    email                  VARCHAR(128),
    latest_sign_in         TIMESTAMP WITHOUT TIME ZONE,
    user_status            SMALLINT                    NOT NULL,
    version                INTEGER,
    sort                   BIGINT,
    avatar                 BIGINT,
    deleted                SMALLINT                    DEFAULT 0,
    latest_change_password TIMESTAMP WITHOUT TIME ZONE
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_username ON sys_user (username);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_identifier ON sys_user (identifier);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_mobile_phone ON sys_user (mobile_phone) WHERE mobile_phone IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_email ON sys_user (email) WHERE email IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_sys_user_status ON sys_user (user_status);
CREATE INDEX IF NOT EXISTS idx_sys_user_deleted ON sys_user (deleted);
CREATE INDEX IF NOT EXISTS idx_sys_user_latest_sign_in ON sys_user (latest_sign_in DESC);

CREATE TABLE IF NOT EXISTS sys_permission_group_ref_permission
(
    permission_group_id BIGINT NOT NULL,
    permission_id       BIGINT NOT NULL,
    PRIMARY KEY (permission_group_id, permission_id),
    CONSTRAINT fk_pg_permission_group FOREIGN KEY (permission_group_id) REFERENCES sys_permission_group (id) ON DELETE CASCADE,
    CONSTRAINT fk_pg_permission FOREIGN KEY (permission_id) REFERENCES sys_permission (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS sys_role_ref_permission_group
(
    role_id             BIGINT NOT NULL,
    permission_group_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_group_id),
    CONSTRAINT fk_role_permission_group_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permission_group_group FOREIGN KEY (permission_group_id) REFERENCES sys_permission_group (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS sys_user_ref_role
(
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE CASCADE
);

-- Column comments (English)
COMMENT ON COLUMN sys_permission.name IS 'Resource name';
COMMENT ON COLUMN sys_permission.resource IS 'Resource module';
COMMENT ON COLUMN sys_permission.action IS 'Permission identifier';
COMMENT ON COLUMN sys_permission.description IS 'Permission description';
COMMENT ON COLUMN sys_permission.deleted IS 'Soft delete flag';
COMMENT ON COLUMN sys_permission.sort IS 'Sort order';

COMMENT ON COLUMN sys_permission_group.name IS 'Permission group name';
COMMENT ON COLUMN sys_permission_group.code IS 'Permission group code';
COMMENT ON COLUMN sys_permission_group.description IS 'Permission group description';

COMMENT ON COLUMN sys_role.name IS 'Role name';
COMMENT ON COLUMN sys_role.code IS 'Role code';
COMMENT ON COLUMN sys_role.status IS 'Role status (0=disabled, 1=enabled)';
COMMENT ON COLUMN sys_role.deleted IS 'Soft delete flag';

COMMENT ON COLUMN sys_user.username IS 'Username';
COMMENT ON COLUMN sys_user.identifier IS 'User unique identifier';
COMMENT ON COLUMN sys_user.mobile_phone IS 'Mobile phone';
COMMENT ON COLUMN sys_user.nickname IS 'Nickname';
COMMENT ON COLUMN sys_user.password IS 'Password';
COMMENT ON COLUMN sys_user.avatar IS 'Avatar';
COMMENT ON COLUMN sys_user.email IS 'Email';
COMMENT ON COLUMN sys_user.user_status IS 'User status (0=disabled, 1=enabled)';
COMMENT ON COLUMN sys_user.deleted IS 'Soft delete flag';
COMMENT ON COLUMN sys_user.latest_sign_in IS 'Last sign-in time';
COMMENT ON COLUMN sys_user.latest_change_password IS 'Last password change time';