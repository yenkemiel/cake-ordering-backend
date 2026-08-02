-- ============================================================
-- 線上蛋糕訂購系統（WishCake）｜seed-test-data.sql
-- 依「開發日誌-8｜模組04 product（種子資料與分類擴充）」§2.2～2.3 定案內容
-- 訂單模組測試情境依「開發日誌-10｜模組06 order（Testcontainers 整合測試）」§3 追加
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
-- 內容：5 分類、11 商品、15 筆變體，涵蓋正常多尺寸、混合上下架、
--       缺貨但仍上架、單一尺寸、全部下架、無尺寸差異（配件／小甜點）、
--       單一商品的分類等測試情境；另追加訂單模組測試用 1 商品、1 變體，
--       涵蓋變體／商品軟刪除情境（見第 4 節）。
-- ============================================================

-- ============================================================
-- 1. products（商品）
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
    ('杜拜巧克力Q餅',   5, '中東風味手工巧克力，內餡開心果醬與酥脆卡達果絲。',             'https://placehold.co/600x450?text=Dubai+Chocolate',      0, NOW(), NOW());

-- ============================================================
-- 2. product_variants（商品變體）
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

-- 杜拜巧克力Q餅（無尺寸差異，其他分類單一商品）
INSERT INTO product_variants (product_id, size, price, stock, status, version, is_deleted, created_at, updated_at)
SELECT id, NULL, 90, 6, 'ACTIVE', 0, 0, NOW(), NOW() FROM products WHERE name = '杜拜巧克力Q餅';

-- ============================================================
-- 3. 商品照片替換：由 placehold.co 佔位圖換成真實照片
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
UPDATE products SET image_url = 'https://duk.tw/THttBd.webp' WHERE name = '杜拜巧克力Q餅';

-- ============================================================
-- 4. 訂單模組測試種子資料（VARIANT_NOT_FOUND 軟刪除情境）
-- 依「開發日誌-10｜模組06 order（Testcontainers 整合測試）」§3 追加，
-- 涵蓋「變體本身已被軟刪除」與「所屬商品已被軟刪除」兩種原本無種子
-- 資料可測的情境
-- ============================================================

-- 訂單模組測試：variant 已軟刪除情境（VARIANT_NOT_FOUND）
UPDATE product_variants SET is_deleted = 1
WHERE product_id = (SELECT id FROM products WHERE name = '藍莓重乳酪蛋糕')
  AND size = '6吋';

-- 訂單模組測試：所屬商品已軟刪除情境（VARIANT_NOT_FOUND）
INSERT INTO products (name, category_id, description, image_url, is_deleted, created_at, updated_at)
VALUES ('測試用已刪除商品', 5, '測試用', 'https://placehold.co/600x450?text=Deleted', 1, NOW(), NOW());

INSERT INTO product_variants (product_id, size, price, stock, status, version, is_deleted, created_at, updated_at)
SELECT id, '6吋', 500, 5, 'ACTIVE', 0, 0, NOW(), NOW()
FROM products WHERE name = '測試用已刪除商品';

-- ============================================================
-- 5. 訂單管理後台展示用種子資料（32 筆，Week 4 orders.html 用）
-- 依討論定案：COMPLETED 14／PENDING 8／SHIPPED 6／CANCELLED 4
-- 用途：讓後台訂單列表在 Demo／開發階段有真實可看的資料量與分頁效果，
-- 純展示用途，直接寫入 orders／order_items，不觸發扣庫存邏輯，
-- 不影響既有商品模組測試情境（stock=0／VARIANT_NOT_FOUND 等）。
-- 執行方式：需在同一個資料庫連線／同一次 Execute SQL Script 內連續執行，
-- 依賴 LAST_INSERT_ID() 取得剛建立的 order id 供 order_items 使用。
-- ============================================================

-- 訂單 1/32：SHIPPED
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202608010914009785', '鄭彥廷', '0954235116', 'customer001@example-mail.com', 'DELIVERY', 'ONLINE_PAYMENT', '台灣某市某區忠孝路177號10樓', 250, '2026-08-06', '希望盒子附提袋', 1030, 'SHIPPED', '2026-08-01 09:14:00', '2026-08-01 09:14:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 780, 1, 780 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '原味巴斯克' AND pv.size = '6吋' LIMIT 1;

-- 訂單 2/32：COMPLETED
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202607192307003664', '李佳玲', '0959310341', 'customer002@example-mail.com', 'DELIVERY', 'ONLINE_PAYMENT', '台灣某市某區民生路195號5樓', 250, '2026-07-23', '無備註', 4600, 'COMPLETED', '2026-07-19 23:07:00', '2026-07-19 23:07:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '8吋', 950, 3, 2850 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '原味千層' AND pv.size = '8吋' LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 750, 2, 1500 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '抹茶千層' AND pv.size = '6吋' LIMIT 1;

-- 訂單 3/32：COMPLETED
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202607170407004752', '邱建宏', '0992832764', 'customer003@example-mail.com', 'PICKUP', 'ONLINE_PAYMENT', NULL, 0, '2026-07-19', '蛋糕請寫『生日快樂』', 1900, 'COMPLETED', '2026-07-17 04:07:00', '2026-07-17 04:07:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '8吋', 950, 2, 1900 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '原味千層' AND pv.size = '8吋' LIMIT 1;

-- 訂單 4/32：PENDING
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202608010033006930', '林佳穎', '0995376724', 'customer004@example-mail.com', 'DELIVERY', 'ONLINE_PAYMENT', '台灣某市某區和平路288號9樓', 250, '2026-08-06', '麻煩幫忙附蠟燭', 2410, 'PENDING', '2026-08-01 00:33:00', '2026-08-01 00:33:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 720, 3, 2160 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '草莓奶油蛋糕' AND pv.size = '6吋' LIMIT 1;

-- 訂單 5/32：SHIPPED
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202607300525002876', '林怡君', '0912269166', 'customer005@example-mail.com', 'PICKUP', 'STORE_PAYMENT', NULL, 0, '2026-08-01', '蛋糕請寫『生日快樂』', 3210, 'SHIPPED', '2026-07-30 05:25:00', '2026-07-30 05:25:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, NULL, 90, 3, 270 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '杜拜巧克力Q餅' AND pv.size IS NULL LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '8吋', 1080, 2, 2160 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '抹茶千層' AND pv.size = '8吋' LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 780, 1, 780 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '原味巴斯克' AND pv.size = '6吋' LIMIT 1;

-- 訂單 6/32：COMPLETED
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202607250412009837', '吳冠宇', '0970482814', 'customer006@example-mail.com', 'PICKUP', 'ONLINE_PAYMENT', NULL, 0, '2026-07-28', '蛋糕請寫『生日快樂』', 3130, 'COMPLETED', '2026-07-25 04:12:00', '2026-07-25 04:12:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 680, 1, 680 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '綜合水果蛋糕' AND pv.size = '6吋' LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '8吋', 950, 1, 950 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '原味千層' AND pv.size = '8吋' LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 750, 2, 1500 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '抹茶千層' AND pv.size = '6吋' LIMIT 1;

-- 訂單 7/32：SHIPPED
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202608010105004470', '張怡君', '0939117182', 'customer007@example-mail.com', 'DELIVERY', 'ONLINE_PAYMENT', '台灣某市某區自由路282號3樓', 250, '2026-08-06', '麻煩幫忙附蠟燭', 5920, 'SHIPPED', '2026-08-01 01:05:00', '2026-08-01 01:05:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 750, 3, 2250 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '抹茶千層' AND pv.size = '6吋' LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '8吋', 1080, 3, 3240 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '抹茶千層' AND pv.size = '8吋' LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, NULL, 90, 2, 180 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '杜拜巧克力Q餅' AND pv.size IS NULL LIMIT 1;

-- 訂單 8/32：COMPLETED
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202607180700001964', '邱冠廷', '0971331509', 'customer008@example-mail.com', 'PICKUP', 'ONLINE_PAYMENT', NULL, 0, '2026-07-20', '蛋糕請寫『生日快樂』', 980, 'COMPLETED', '2026-07-18 07:00:00', '2026-07-18 07:00:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '8吋', 980, 1, 980 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '綜合水果蛋糕' AND pv.size = '8吋' LIMIT 1;

-- 訂單 9/32：PENDING
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202607280209004119', '林冠廷', '0934738299', 'customer009@example-mail.com', 'PICKUP', 'ONLINE_PAYMENT', NULL, 0, '2026-08-02', '麻煩幫忙附蠟燭', 2940, 'PENDING', '2026-07-28 02:09:00', '2026-07-28 02:09:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '8吋', 980, 3, 2940 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '綜合水果蛋糕' AND pv.size = '8吋' LIMIT 1;

-- 訂單 10/32：COMPLETED
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202607200733005563', '王彥廷', '0967010651', 'customer010@example-mail.com', 'DELIVERY', 'ONLINE_PAYMENT', '台灣某市某區和平路98號9樓', 250, '2026-07-23', NULL, 4690, 'COMPLETED', '2026-07-20 07:33:00', '2026-07-20 07:33:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 750, 2, 1500 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '抹茶千層' AND pv.size = '6吋' LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '8吋', 980, 3, 2940 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '綜合水果蛋糕' AND pv.size = '8吋' LIMIT 1;

-- 訂單 11/32：PENDING
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202608020400005345', '陳佩珊', '0901326773', 'customer011@example-mail.com', 'PICKUP', 'ONLINE_PAYMENT', NULL, 0, '2026-08-07', '希望盒子附提袋', 5580, 'PENDING', '2026-08-02 04:00:00', '2026-08-02 04:00:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '8吋', 1080, 3, 3240 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '抹茶千層' AND pv.size = '8吋' LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 780, 3, 2340 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '原味巴斯克' AND pv.size = '6吋' LIMIT 1;

-- 訂單 12/32：COMPLETED
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202607270923009238', '許宜蓁', '0923430980', 'customer012@example-mail.com', 'PICKUP', 'STORE_PAYMENT', NULL, 0, '2026-08-01', '希望盒子附提袋', 2480, 'COMPLETED', '2026-07-27 09:23:00', '2026-07-27 09:23:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 720, 1, 720 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '草莓奶油蛋糕' AND pv.size = '6吋' LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 680, 1, 680 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '綜合水果蛋糕' AND pv.size = '6吋' LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '8吋', 1080, 1, 1080 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '抹茶千層' AND pv.size = '8吋' LIMIT 1;

-- 訂單 13/32：CANCELLED
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202607190401004346', '林柏翰', '0961939909', 'customer013@example-mail.com', 'DELIVERY', 'ONLINE_PAYMENT', '台灣某市某區建國路299號10樓', 250, '2026-07-23', '無備註', 5060, 'CANCELLED', '2026-07-19 04:01:00', '2026-07-19 04:01:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '8吋', 950, 2, 1900 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '原味千層' AND pv.size = '8吋' LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 750, 1, 750 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '抹茶千層' AND pv.size = '6吋' LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 720, 3, 2160 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '草莓奶油蛋糕' AND pv.size = '6吋' LIMIT 1;

-- 訂單 14/32：PENDING
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202607300552002127', '李家豪', '0951079911', 'customer014@example-mail.com', 'PICKUP', 'ONLINE_PAYMENT', NULL, 0, '2026-08-03', NULL, 1900, 'PENDING', '2026-07-30 05:52:00', '2026-07-30 05:52:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '8吋', 950, 2, 1900 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '原味千層' AND pv.size = '8吋' LIMIT 1;

-- 訂單 15/32：SHIPPED
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202608030151005616', '劉佩珊', '0949808412', 'customer015@example-mail.com', 'PICKUP', 'ONLINE_PAYMENT', NULL, 0, '2026-08-07', '希望盒子附提袋', 7060, 'SHIPPED', '2026-08-03 01:51:00', '2026-08-03 01:51:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 750, 3, 2250 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '抹茶千層' AND pv.size = '6吋' LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '8吋', 950, 3, 2850 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '原味千層' AND pv.size = '8吋' LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '8吋', 980, 2, 1960 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '綜合水果蛋糕' AND pv.size = '8吋' LIMIT 1;

-- 訂單 16/32：PENDING
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202608020225001158', '劉品妍', '0901640052', 'customer016@example-mail.com', 'PICKUP', 'STORE_PAYMENT', NULL, 0, '2026-08-07', NULL, 2940, 'PENDING', '2026-08-02 02:25:00', '2026-08-02 02:25:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '8吋', 980, 3, 2940 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '綜合水果蛋糕' AND pv.size = '8吋' LIMIT 1;

-- 訂單 17/32：CANCELLED
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202607130447002684', '蔡怡君', '0959826204', 'customer017@example-mail.com', 'PICKUP', 'ONLINE_PAYMENT', NULL, 0, '2026-07-16', NULL, 2610, 'CANCELLED', '2026-07-13 04:47:00', '2026-07-13 04:47:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, NULL, 90, 3, 270 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '杜拜巧克力Q餅' AND pv.size IS NULL LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 780, 3, 2340 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '原味巴斯克' AND pv.size = '6吋' LIMIT 1;

-- 訂單 18/32：COMPLETED
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202607150836008711', '邱柏翰', '0922602563', 'customer018@example-mail.com', 'PICKUP', 'ONLINE_PAYMENT', NULL, 0, '2026-07-17', '蛋糕請寫『生日快樂』', 1500, 'COMPLETED', '2026-07-15 08:36:00', '2026-07-15 08:36:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 750, 2, 1500 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '抹茶千層' AND pv.size = '6吋' LIMIT 1;

-- 訂單 19/32：COMPLETED
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202607170139002889', '李柏翰', '0930365414', 'customer019@example-mail.com', 'PICKUP', 'STORE_PAYMENT', NULL, 0, '2026-07-19', '希望盒子附提袋', 1800, 'COMPLETED', '2026-07-17 01:39:00', '2026-07-17 01:39:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 720, 1, 720 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '草莓奶油蛋糕' AND pv.size = '6吋' LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '8吋', 1080, 1, 1080 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '抹茶千層' AND pv.size = '8吋' LIMIT 1;

-- 訂單 20/32：PENDING
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202607290400006967', '吳俊傑', '0956981693', 'customer020@example-mail.com', 'PICKUP', 'ONLINE_PAYMENT', NULL, 0, '2026-08-01', '無備註', 4840, 'PENDING', '2026-07-29 04:00:00', '2026-07-29 04:00:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '8吋', 980, 3, 2940 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '綜合水果蛋糕' AND pv.size = '8吋' LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '8吋', 950, 2, 1900 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '原味千層' AND pv.size = '8吋' LIMIT 1;

-- 訂單 21/32：COMPLETED
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202607202313005930', '洪志豪', '0948465648', 'customer021@example-mail.com', 'DELIVERY', 'ONLINE_PAYMENT', '台灣某市某區和平路216號11樓', 250, '2026-07-23', NULL, 1790, 'COMPLETED', '2026-07-20 23:13:00', '2026-07-20 23:13:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, NULL, 90, 2, 180 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '杜拜巧克力Q餅' AND pv.size IS NULL LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 680, 2, 1360 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '綜合水果蛋糕' AND pv.size = '6吋' LIMIT 1;

-- 訂單 22/32：PENDING
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202607290842002530', '吳子軒', '0995777387', 'customer022@example-mail.com', 'PICKUP', 'ONLINE_PAYMENT', NULL, 0, '2026-08-02', NULL, 1080, 'PENDING', '2026-07-29 08:42:00', '2026-07-29 08:42:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '8吋', 1080, 1, 1080 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '抹茶千層' AND pv.size = '8吋' LIMIT 1;

-- 訂單 23/32：PENDING
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202608020719002746', '黃雅婷', '0903791769', 'customer023@example-mail.com', 'DELIVERY', 'ONLINE_PAYMENT', '台灣某市某區建國路254號7樓', 250, '2026-08-04', '麻煩幫忙附蠟燭', 4660, 'PENDING', '2026-08-02 07:19:00', '2026-08-02 07:19:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 750, 3, 2250 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '抹茶千層' AND pv.size = '6吋' LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 720, 3, 2160 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '草莓奶油蛋糕' AND pv.size = '6吋' LIMIT 1;

-- 訂單 24/32：COMPLETED
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202607180133003607', '陳佩珊', '0931727889', 'customer024@example-mail.com', 'PICKUP', 'STORE_PAYMENT', NULL, 0, '2026-07-23', '希望盒子附提袋', 1770, 'COMPLETED', '2026-07-18 01:33:00', '2026-07-18 01:33:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, NULL, 60, 1, 60 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '造型蠟燭' AND pv.size IS NULL LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, NULL, 90, 3, 270 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '杜拜巧克力Q餅' AND pv.size IS NULL LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 720, 2, 1440 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '草莓奶油蛋糕' AND pv.size = '6吋' LIMIT 1;

-- 訂單 25/32：CANCELLED
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202607250746003503', '劉柏翰', '0947143455', 'customer025@example-mail.com', 'PICKUP', 'ONLINE_PAYMENT', NULL, 0, '2026-07-30', '麻煩幫忙附蠟燭', 3730, 'CANCELLED', '2026-07-25 07:46:00', '2026-07-25 07:46:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 750, 2, 1500 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '抹茶千層' AND pv.size = '6吋' LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '8吋', 980, 2, 1960 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '綜合水果蛋糕' AND pv.size = '8吋' LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, NULL, 90, 3, 270 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '杜拜巧克力Q餅' AND pv.size IS NULL LIMIT 1;

-- 訂單 26/32：COMPLETED
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202607140326008999', '吳怡君', '0936690967', 'customer026@example-mail.com', 'DELIVERY', 'ONLINE_PAYMENT', '台灣某市某區忠孝路153號7樓', 250, '2026-07-17', '麻煩幫忙附蠟燭', 2410, 'COMPLETED', '2026-07-14 03:26:00', '2026-07-14 03:26:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '8吋', 1080, 2, 2160 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '抹茶千層' AND pv.size = '8吋' LIMIT 1;

-- 訂單 27/32：SHIPPED
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202608022333008564', '陳思妤', '0956272980', 'customer027@example-mail.com', 'PICKUP', 'ONLINE_PAYMENT', NULL, 0, '2026-08-05', '麻煩幫忙附蠟燭', 1360, 'SHIPPED', '2026-08-02 23:33:00', '2026-08-02 23:33:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 680, 2, 1360 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '綜合水果蛋糕' AND pv.size = '6吋' LIMIT 1;

-- 訂單 28/32：CANCELLED
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202607160157004673', '王佳穎', '0975564641', 'customer028@example-mail.com', 'PICKUP', 'ONLINE_PAYMENT', NULL, 0, '2026-07-20', NULL, 2440, 'CANCELLED', '2026-07-16 01:57:00', '2026-07-16 01:57:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '8吋', 980, 1, 980 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '綜合水果蛋糕' AND pv.size = '8吋' LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 680, 1, 680 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '綜合水果蛋糕' AND pv.size = '6吋' LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 780, 1, 780 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '原味巴斯克' AND pv.size = '6吋' LIMIT 1;

-- 訂單 29/32：COMPLETED
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202607180013003683', '楊詩涵', '0932719374', 'customer029@example-mail.com', 'PICKUP', 'ONLINE_PAYMENT', NULL, 0, '2026-07-20', '無備註', 4000, 'COMPLETED', '2026-07-18 00:13:00', '2026-07-18 00:13:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '8吋', 980, 2, 1960 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '綜合水果蛋糕' AND pv.size = '8吋' LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 680, 3, 2040 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '綜合水果蛋糕' AND pv.size = '6吋' LIMIT 1;

-- 訂單 30/32：SHIPPED
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202607302337009289', '邱思妤', '0963193149', 'customer030@example-mail.com', 'DELIVERY', 'ONLINE_PAYMENT', '台灣某市某區中山路178號9樓', 250, '2026-08-01', '麻煩幫忙附蠟燭', 4290, 'SHIPPED', '2026-07-30 23:37:00', '2026-07-30 23:37:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '8吋', 950, 2, 1900 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '原味千層' AND pv.size = '8吋' LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 680, 2, 1360 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '綜合水果蛋糕' AND pv.size = '6吋' LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 780, 1, 780 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '原味巴斯克' AND pv.size = '6吋' LIMIT 1;

-- 訂單 31/32：COMPLETED
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202607140043005022', '王家豪', '0926284987', 'customer031@example-mail.com', 'PICKUP', 'STORE_PAYMENT', NULL, 0, '2026-07-18', NULL, 2160, 'COMPLETED', '2026-07-14 00:43:00', '2026-07-14 00:43:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '8吋', 1080, 2, 2160 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '抹茶千層' AND pv.size = '8吋' LIMIT 1;

-- 訂單 32/32：COMPLETED
INSERT INTO orders (order_no, customer_name, phone, email, shipping_method, payment_method, address, shipping_fee, pickup_date, remark, total_amount, status, created_at, updated_at) VALUES ('ORD202607220539005526', '謝家豪', '0999650752', 'customer032@example-mail.com', 'PICKUP', 'ONLINE_PAYMENT', NULL, 0, '2026-07-26', '麻煩幫忙附蠟燭', 3110, 'COMPLETED', '2026-07-22 05:39:00', '2026-07-22 05:39:00');
SET @oid := LAST_INSERT_ID();
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 680, 1, 680 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '綜合水果蛋糕' AND pv.size = '6吋' LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, '6吋', 750, 3, 2250 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '抹茶千層' AND pv.size = '6吋' LIMIT 1;
INSERT INTO order_items (order_id, variant_id, product_name, variant_size, unit_price, quantity, subtotal) SELECT @oid, pv.id, p.name, NULL, 90, 2, 180 FROM product_variants pv JOIN products p ON pv.product_id = p.id WHERE p.name = '杜拜巧克力Q餅' AND pv.size IS NULL LIMIT 1;