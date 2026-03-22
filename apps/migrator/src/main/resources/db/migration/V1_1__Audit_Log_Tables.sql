CREATE TABLE IF NOT EXISTS sys_login_log
(
    id         BIGSERIAL PRIMARY KEY,
    create_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    update_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    create_by  VARCHAR(128),
    update_by  VARCHAR(128),
    username   VARCHAR(128),
    success    BOOLEAN                     NOT NULL,
    reason     VARCHAR(255),
    remote_ip  VARCHAR(128),
    user_agent VARCHAR(512),
    login_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_sys_login_log_username ON sys_login_log (username);
CREATE INDEX IF NOT EXISTS idx_sys_login_log_login_at ON sys_login_log (login_at DESC);
CREATE INDEX IF NOT EXISTS idx_sys_login_log_success ON sys_login_log (success);

CREATE TABLE IF NOT EXISTS sys_operation_log
(
    id                    BIGSERIAL PRIMARY KEY,
    create_at             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    update_at             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    create_by             VARCHAR(128),
    update_by             VARCHAR(128),
    operator              VARCHAR(128),
    operator_display_name VARCHAR(255),
    operator_type         VARCHAR(64),
    module                VARCHAR(128),
    action                VARCHAR(128),
    target                VARCHAR(255),
    success               BOOLEAN                     NOT NULL,
    detail                VARCHAR(2000),
    remote_ip             VARCHAR(128),
    user_agent            VARCHAR(512),
    request_id            VARCHAR(128),
    occurred_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_sys_operation_log_operator ON sys_operation_log (operator);
CREATE INDEX IF NOT EXISTS idx_sys_operation_log_module_action ON sys_operation_log (module, action);
CREATE INDEX IF NOT EXISTS idx_sys_operation_log_occurred_at ON sys_operation_log (occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_sys_operation_log_request_id ON sys_operation_log (request_id);
