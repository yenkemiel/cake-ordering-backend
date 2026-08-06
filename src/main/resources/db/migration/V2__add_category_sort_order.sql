-- ============================================================
-- 對應原始 commit 95d1c07：categories 新增 sort_order 欄位，支援分類拖曳排序持久化
-- ============================================================

ALTER TABLE categories
    ADD COLUMN sort_order INT NOT NULL DEFAULT 0 AFTER name;

UPDATE categories SET sort_order = 1 WHERE name = '水果系列蛋糕';
UPDATE categories SET sort_order = 2 WHERE name = '千層系列蛋糕';
UPDATE categories SET sort_order = 3 WHERE name = '起司系列蛋糕';
UPDATE categories SET sort_order = 4 WHERE name = '配件';
