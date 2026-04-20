CREATE TABLE components (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(100)   NOT NULL,
    component_type VARCHAR(50)    NOT NULL,
    wood_type      VARCHAR(30),
    unit_price     NUMERIC(10, 2) NOT NULL,
    description    VARCHAR(500)
);
