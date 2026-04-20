CREATE TABLE guitar_specs (
    id             BIGSERIAL PRIMARY KEY,
    body_type      VARCHAR(30)   NOT NULL,
    body_wood      VARCHAR(30)   NOT NULL,
    neck_wood      VARCHAR(30)   NOT NULL,
    fretboard_wood VARCHAR(30)   NOT NULL,
    string_count   VARCHAR(10)   NOT NULL,
    pickup_type    VARCHAR(20)   NOT NULL,
    finish         VARCHAR(10)   NOT NULL,
    scale_length   NUMERIC(4, 2) NOT NULL,
    color          VARCHAR(50)   NOT NULL
);
