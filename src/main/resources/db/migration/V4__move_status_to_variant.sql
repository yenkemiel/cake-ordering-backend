-- ============================================================
-- 對應原始 commit afbd127：上下架欄位搬到 product_variants，products 移除 status
-- ============================================================

ALTER TABLE product_variants
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' AFTER stock;

UPDATE product_variants pv
JOIN products p ON pv.product_id = p.id
SET pv.status = p.status;

ALTER TABLE product_variants
    ADD KEY idx_variants_status_deleted (is_deleted, status);

ALTER TABLE products
    DROP INDEX idx_products_list;

ALTER TABLE products
    DROP COLUMN status;

ALTER TABLE products
    ADD KEY idx_products_list (is_deleted, category_id);
