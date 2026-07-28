-- ============================================================
-- 線上蛋糕訂購系統（WishCake）｜seed-test-data.sql
-- 依「開發日誌-8｜模組04 product（種子資料與分類擴充）」§2.2～2.3 定案內容
--
-- 用途：本機開發測試用假資料（商品＋變體），非正式環境必要種子資料，
--       不受 Flyway 管理，不會被自動套用，內容預期會隨測試需求調整。
--
-- 依賴：須先執行 init.sql／Flyway migration（V1__init.sql）建立
--       categories 之後才能執行本檔案（category_id 對應：
--       1=水果系列蛋糕 2=千層系列蛋糕 3=起司系列蛋糕 4=配件 5=其他）。
--
-- 執行方式：
--   mysql -h 127.0.0.1 -P 3306 -u cake_app -p cake_ordering_db < sql/seed-test-data.sql
--
-- 內容：5 分類、11 商品、19 筆變體，涵蓋正常多尺寸、混合上下架、
--       缺貨但仍上架、單一尺寸、全部下架、無尺寸差異（配件／小甜點）、
--       單一商品的分類等測試情境。
-- ============================================================

-- ============================================================
-- products（商品）
-- ============================================================
INSERT INTO products (name, category_id, description, image_url, is_deleted, created_at, updated_at) VALUES
    ('綜合水果蛋糕',   1, '嚴選當季新鮮水果，酸甜平衡，適合各種慶祝場合。',           'https://placehold.co/600x450?text=Fruit+Mix+Cake',       0, NOW(), NOW()),
    ('草莓奶油蛋糕',   1, '日本進口草莓搭配輕盈鮮奶油，清爽不甜膩。',                 'https://placehold.co/600x450?text=Strawberry+Cake',      0, NOW(), NOW()),
    ('芒果生乳酪蛋糕', 1, '濃郁芒果果泥融合生乳酪，入口即化。',                       'https://placehold.co/600x450?text=Mango+Cheesecake',     0, NOW(), NOW()),
    ('抹茶千層',       2, '手工製作二十層以上薄餅皮，搭配宇治抹茶奶餡。',             'https://placehold.co/600x450?text=Matcha+Mille+Crepe',   0, NOW(), NOW()),
    ('原味千層',       2, '經典原味千層，層層堆疊的細緻口感。',                       'https://placehold.co/600x450?text=Original+Mille+Crepe', 0, NOW(), NOW()),
    ('巧克力千層',     2, '比利時巧克力融入千層餅皮，濃郁不膩口。',                   'https://placehold.co/600x450?text=Chocolate+Mille+Crepe',0, NOW(), NOW()),
    ('原味巴斯克',     3, '外層微焦、中心綿密濕潤的經典巴斯克起司蛋糕。',             'https://placehold.co/600x450?text=Basque+Cheesecake',    0, NOW(), NOW()),
    ('藍莓重乳酪蛋糕', 3, '濃郁重乳酪搭配藍莓果醬，口感綿密扎實。',                   'https://placehold.co/600x450?text=Blueberry+Cheesecake', 0, NOW(), NOW()),
    ('造型蠟燭',       4, '各式造型生日蠟燭，可依需求選購。',                         'https://placehold.co/600x450?text=Candle',               0, NOW(), NOW()),
    ('生日卡片',       4, '手工設計生日卡片，可搭配蛋糕加購。',                       'https://placehold.co/600x450?text=Birthday+Card',        0, NOW(), NOW()),
    ('杜拜巧克力',     5, '中東風味手工巧克力，內餡開心果醬與酥脆卡達果絲。',         'https://placehold.co/600x450?text=Dubai+Chocolate',      0, NOW(), NOW());

-- ============================================================
-- product_variants（商品變體）
-- ============================================================

-- 綜合水果蛋糕（正常多尺寸，全部 ACTIVE）
INSERT INTO product_variants (product_id, size, price, stock, status, version, is_deleted, created_at, updated_at)
SELECT id, '6吋', 680, 10, 'ACTIVE', 0, 0, NOW(), NOW() FROM products WHERE name = '綜合水果蛋糕';
INSERT INTO product_variants (product_id, size, price, stock, status, version, is_deleted, created_at, updated_at)
SELECT id, '8吋', 980, 5, 'ACTIVE', 0, 0, NOW(), NOW() FROM products WHERE name = '綜合水果蛋糕';

-- 草莓奶油蛋糕（混合上下架）
INSERT INTO product_variants (product_id, size, price, stock, status, version, is_deleted, created_at, updated_at)
SELECT id, '6吋', 720, 8, 'ACTIVE', 0, 0, NOW(), NOW() FROM products WHERE name = '草莓奶油蛋糕';
INSERT INTO product_variants (product_id, size, price, stock, status, version, is_deleted, created_at, updated_at)
SELECT id, '8吋', 1050, 3, 'INACTIVE', 0, 0, NOW(), NOW() FROM products WHERE name = '草莓奶油蛋糕';

-- 芒果生乳酪蛋糕（缺貨但仍上架，stock=0 不等於 INACTIVE）
INSERT INTO product_variants (product_id, size, price, stock, status, version, is_deleted, created_at, updated_at)
SELECT id, '6吋', 650, 0, 'ACTIVE', 0, 0, NOW(), NOW() FROM products WHERE name = '芒果生乳酪蛋糕';

-- 抹茶千層（正常多尺寸，全部 ACTIVE）
INSERT INTO product_variants (product_id, size, price, stock, status, version, is_deleted, created_at, updated_at)
SELECT id, '6吋', 750, 6, 'ACTIVE', 0, 0, NOW(), NOW() FROM products WHERE name = '抹茶千層';
INSERT INTO product_variants (product_id, size, price, stock, status, version, is_deleted, created_at, updated_at)
SELECT id, '8吋', 1080, 4, 'ACTIVE', 0, 0, NOW(), NOW() FROM products WHERE name = '抹茶千層';

-- 原味千層（單一尺寸正常）
INSERT INTO product_variants (product_id, size, price, stock, status, version, is_deleted, created_at, updated_at)
SELECT id, '8吋', 950, 10, 'ACTIVE', 0, 0, NOW(), NOW() FROM products WHERE name = '原味千層';

-- 巧克力千層（全部下架，前台整張卡不顯示）
INSERT INTO product_variants (product_id, size, price, stock, status, version, is_deleted, created_at, updated_at)
SELECT id, '6吋', 700, 5, 'INACTIVE', 0, 0, NOW(), NOW() FROM products WHERE name = '巧克力千層';
INSERT INTO product_variants (product_id, size, price, stock, status, version, is_deleted, created_at, updated_at)
SELECT id, '8吋', 1020, 3, 'INACTIVE', 0, 0, NOW(), NOW() FROM products WHERE name = '巧克力千層';

-- 原味巴斯克（單一尺寸正常）
INSERT INTO product_variants (product_id, size, price, stock, status, version, is_deleted, created_at, updated_at)
SELECT id, '6吋', 780, 7, 'ACTIVE', 0, 0, NOW(), NOW() FROM products WHERE name = '原味巴斯克';

-- 藍莓重乳酪蛋糕（單一尺寸正常）
INSERT INTO product_variants (product_id, size, price, stock, status, version, is_deleted, created_at, updated_at)
SELECT id, '6吋', 690, 9, 'ACTIVE', 0, 0, NOW(), NOW() FROM products WHERE name = '藍莓重乳酪蛋糕';

-- 造型蠟燭（無尺寸差異）
INSERT INTO product_variants (product_id, size, price, stock, status, version, is_deleted, created_at, updated_at)
SELECT id, NULL, 60, 20, 'ACTIVE', 0, 0, NOW(), NOW() FROM products WHERE name = '造型蠟燭';

-- 生日卡片（無尺寸差異＋下架）
INSERT INTO product_variants (product_id, size, price, stock, status, version, is_deleted, created_at, updated_at)
SELECT id, NULL, 45, 15, 'INACTIVE', 0, 0, NOW(), NOW() FROM products WHERE name = '生日卡片';

-- 杜拜巧克力（無尺寸差異，其他分類單一商品）
INSERT INTO product_variants (product_id, size, price, stock, status, version, is_deleted, created_at, updated_at)
SELECT id, NULL, 90, 6, 'ACTIVE', 0, 0, NOW(), NOW() FROM products WHERE name = '杜拜巧克力';

-- ============================================================
-- 商品照片替換：由 placehold.co 佔位圖換成真實照片
-- 圖床：duk.tw（原規劃使用 GitHub raw 連結，因 cake-ordering-frontend
-- repo 為 Private，raw 連結會帶時效性 token，不適合存入需長期穩定的
-- image_url 欄位，改用 duk.tw 永久連結）
-- 圖片原始檔另存於 cake-ordering-frontend repo 的 admin/img/ 資料夾備份
-- ============================================================
UPDATE products SET image_url = 'https://duk.tw/cLSxvj.webp' WHERE name = '綜合水果蛋糕';
UPDATE products SET image_url = 'https://duk.tw/YCYs2B.webp' WHERE name = '草莓奶油蛋糕';
UPDATE products SET image_url = 'https://duk.tw/LSeJwG.webp' WHERE name = '抹茶千層';
UPDATE products SET image_url = 'https://duk.tw/rnb45L.webp' WHERE name = '芒果生乳酪蛋糕';
UPDATE products SET image_url = 'https://duk.tw/MPWk7z.webp' WHERE name = '原味千層';
UPDATE products SET image_url = 'https://duk.tw/5wj5UI.webp' WHERE name = '巧克力千層';
UPDATE products SET image_url = 'https://duk.tw/sBWHyd.webp' WHERE name = '原味巴斯克';
UPDATE products SET image_url = 'https://duk.tw/dCZi0w.webp' WHERE name = '藍莓重乳酪蛋糕';
UPDATE products SET image_url = 'https://duk.tw/okGEqo.webp' WHERE name = '造型蠟燭';
UPDATE products SET image_url = 'https://duk.tw/PbCREK.webp' WHERE name = '生日卡片';
UPDATE products SET image_url = 'https://duk.tw/THttBd.webp' WHERE name = '杜拜巧克力';