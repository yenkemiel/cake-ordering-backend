-- ============================================================
-- 對應原始 commit ace9da7：categories 新增「其他」分類
-- ============================================================

INSERT INTO categories (name, sort_order, created_at, updated_at)
VALUES ('其他', 5, NOW(), NOW());
