-- Sample DDD example schema (product + order). Application uses snowflake IDs; no DB defaults on id.
CREATE TABLE IF NOT EXISTS ex_product
(
    id          BIGINT PRIMARY KEY,
    create_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    update_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    create_by   VARCHAR(128),
    update_by   VARCHAR(128),
    name        VARCHAR(255)                NOT NULL,
    price_minor BIGINT                      NOT NULL,
    stock       INTEGER                     NOT NULL,
    active      BOOLEAN                     NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_ex_product_active ON ex_product (active);

CREATE TABLE IF NOT EXISTS ex_order
(
    id              BIGINT PRIMARY KEY,
    create_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    update_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    create_by       VARCHAR(128),
    update_by       VARCHAR(128),
    buyer_username  VARCHAR(128)                NOT NULL,
    status          VARCHAR(32)                 NOT NULL,
    total_minor     BIGINT                      NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_ex_order_buyer ON ex_order (buyer_username);

CREATE TABLE IF NOT EXISTS ex_order_line
(
    id               BIGINT PRIMARY KEY,
    create_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    update_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    create_by        VARCHAR(128),
    update_by        VARCHAR(128),
    order_id         BIGINT                      NOT NULL,
    product_id       BIGINT                      NOT NULL,
    quantity         INTEGER                     NOT NULL,
    unit_price_minor BIGINT                      NOT NULL,
    CONSTRAINT fk_ex_order_line_order FOREIGN KEY (order_id) REFERENCES ex_order (id) ON DELETE CASCADE,
    CONSTRAINT fk_ex_order_line_product FOREIGN KEY (product_id) REFERENCES ex_product (id)
);

CREATE INDEX IF NOT EXISTS idx_ex_order_line_order ON ex_order_line (order_id);

COMMENT ON TABLE ex_product IS 'DDD example: catalog product';
COMMENT ON TABLE ex_order IS 'DDD example: order placed by a user (buyer_username references identity)';
COMMENT ON TABLE ex_order_line IS 'DDD example: order line items';
