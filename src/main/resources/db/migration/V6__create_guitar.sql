CREATE TABLE guitars (
    id              BIGSERIAL PRIMARY KEY,
    serial_number   VARCHAR(50)    NOT NULL UNIQUE,
    model_id        BIGINT         NOT NULL REFERENCES guitar_models (id),
    spec_id         BIGINT         NOT NULL UNIQUE REFERENCES guitar_specs (id),
    order_id        BIGINT         NOT NULL REFERENCES orders (id),
    status          VARCHAR(20)    NOT NULL,
    estimated_price NUMERIC(10, 2) NOT NULL,
    created_at      TIMESTAMP      NOT NULL,
    completed_at    TIMESTAMP
);
