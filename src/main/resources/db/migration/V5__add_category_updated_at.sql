-- ============================================================
-- 對應原始 commit 1aaf174：categories 表新增 updated_at 欄位
-- ============================================================

ALTER TABLE categories
    ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER created_at;

UPDATE categories SET updated_at = created_at;

ALTER TABLE categories
    ALTER COLUMN updated_at DROP DEFAULT;
