CREATE TABLE IF NOT EXISTS sys_permission
(
    id          BIGSERIAL PRIMARY KEY,
    create_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    update_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    create_by   VARCHAR(128),
    update_by   VARCHAR(128),
    name        VARCHAR(128)                NOT NULL,
    code        VARCHAR(128)                NOT NULL,
    domain      VARCHAR(64)                 NOT NULL,
    resource    VARCHAR(128)                NOT NULL,
    action      VARCHAR(128)                NOT NULL,
    group_code  VARCHAR(128),
    description VARCHAR(255),
    expression  VARCHAR(255)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_permission_name ON sys_permission (name);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_permission_code ON sys_permission (code);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_permission_domain_resource_action ON sys_permission (domain, resource, action);
CREATE INDEX IF NOT EXISTS idx_sys_permission_domain ON sys_permission (domain);
CREATE INDEX IF NOT EXISTS idx_sys_permission_resource ON sys_permission (resource);
CREATE INDEX IF NOT EXISTS idx_sys_permission_action ON sys_permission (action);
CREATE INDEX IF NOT EXISTS idx_sys_permission_group_code ON sys_permission (group_code);
CREATE INDEX IF NOT EXISTS idx_sys_permission_domain_resource ON sys_permission (domain, resource);

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
    sort        INTEGER                     NOT NULL DEFAULT 0
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
    status      VARCHAR(32)                 NOT NULL,
    sort        INTEGER                     NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_name ON sys_role (name);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_role_code ON sys_role (code);
CREATE INDEX IF NOT EXISTS idx_sys_role_status ON sys_role (status);
CREATE INDEX IF NOT EXISTS idx_sys_role_sort ON sys_role (sort);

CREATE TABLE IF NOT EXISTS sys_user
(
    id              BIGSERIAL PRIMARY KEY,
    create_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    update_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    create_by       VARCHAR(128),
    update_by       VARCHAR(128),
    username        VARCHAR(128)                NOT NULL,
    password        VARCHAR(255)                NOT NULL,
    identifier      VARCHAR(128)                NOT NULL,
    mobile_phone    VARCHAR(64),
    nickname        VARCHAR(128),
    email           VARCHAR(128),
    latest_sign_in  TIMESTAMP WITHOUT TIME ZONE,
    user_status     VARCHAR(32)                 NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_username ON sys_user (username);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_identifier ON sys_user (identifier);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_mobile_phone ON sys_user (mobile_phone) WHERE mobile_phone IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_user_email ON sys_user (email) WHERE email IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_sys_user_status ON sys_user (user_status);

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
