CREATE TABLE inventory_items (
    id                 BIGSERIAL PRIMARY KEY,
    component_id       BIGINT  NOT NULL REFERENCES components (id),
    quantity_in_stock  INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_quantity_non_negative CHECK (quantity_in_stock >= 0)
);
