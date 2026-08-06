-- ============================================================
-- 對應原始 commit e5817e9：商品資料表改為 Product-Variant 模型
--
-- 注意：本步驟把 order_items.product_id 改名為 variant_id，
-- 但新舊兩個 id 序列並非同一序列（product_variants.id 是全新 AUTO_INCREMENT）。
-- 這在「一定會清空重建」的本機開發資料庫沒有影響，但不是通用的資料安全搬遷手法，
-- 正式環境／保留歷史資料的場景不可比照辦理。
-- ============================================================

CREATE TABLE product_variants (
    id           BIGINT         NOT NULL AUTO_INCREMENT,
    product_id   BIGINT         NOT NULL,
    size         VARCHAR(20)    NULL,
    price        DECIMAL(10,2)  NOT NULL,
    stock        INT            NOT NULL DEFAULT 0,
    version      INT            NOT NULL DEFAULT 0,
    is_deleted   TINYINT(1)     NOT NULL DEFAULT 0,
    created_at   DATETIME       NOT NULL,
    updated_at   DATETIME       NOT NULL,

    PRIMARY KEY (id),

    KEY idx_variants_product_id (product_id),
    KEY idx_variants_product_deleted (product_id, is_deleted),

    CONSTRAINT fk_variants_product
        FOREIGN KEY (product_id) REFERENCES products (id)
        ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

INSERT INTO product_variants (product_id, size, price, stock, version, is_deleted, created_at, updated_at)
SELECT id, size, price, stock, version, is_deleted, created_at, updated_at
FROM products;

ALTER TABLE products
    DROP COLUMN size,
    DROP COLUMN price,
    DROP COLUMN stock,
    DROP COLUMN version;

ALTER TABLE order_items
    DROP FOREIGN KEY fk_order_items_product;

ALTER TABLE order_items
    DROP INDEX idx_order_items_product_id;

ALTER TABLE order_items
    CHANGE COLUMN product_id variant_id BIGINT NOT NULL,
    ADD COLUMN variant_size VARCHAR(20) NULL AFTER product_name;

ALTER TABLE order_items
    ADD KEY idx_order_items_variant_id (variant_id);

ALTER TABLE order_items
    ADD CONSTRAINT fk_order_items_variant
        FOREIGN KEY (variant_id) REFERENCES product_variants (id)
        ON UPDATE CASCADE;
