CREATE TABLE IF NOT EXISTS app_outbox_message
(
    id             BIGINT PRIMARY KEY,
    create_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    update_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    create_by      VARCHAR(128),
    update_by      VARCHAR(128),
    aggregate_type VARCHAR(128)                NOT NULL,
    aggregate_id   VARCHAR(128)                NOT NULL,
    event_type     VARCHAR(255)                NOT NULL,
    payload        TEXT                        NOT NULL,
    occurred_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    available_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    published_at   TIMESTAMP WITHOUT TIME ZONE,
    status         SMALLINT                    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_app_outbox_status_available
    ON app_outbox_message (status, available_at);

CREATE INDEX IF NOT EXISTS idx_app_outbox_aggregate
    ON app_outbox_message (aggregate_type, aggregate_id);

COMMENT ON TABLE app_outbox_message IS 'Application outbox for domain events emitted inside write-side transactions';
