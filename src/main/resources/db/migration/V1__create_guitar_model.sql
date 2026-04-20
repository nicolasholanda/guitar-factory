CREATE TABLE guitar_models (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100)    NOT NULL UNIQUE,
    description VARCHAR(500),
    base_price  NUMERIC(10, 2)  NOT NULL
);
