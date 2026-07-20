-- ============================================================
-- 線上蛋糕訂購系統（WishCake）｜init.sql
-- 依「後端需求書」4.2 節資料表設計、9.3 節種子資料、12.2 節 admin seed 備忘
-- 依「ERD」第 3 章欄位清單、第 4 章外鍵規則、第 5 章索引設計建議、第 6 章種子資料
-- 依「Claude Instructions」命名規範／樂觀鎖／軟刪除／charset 規則
--
-- charset：utf8mb4；collation：utf8mb4_general_ci（不分大小寫，非 _bin/_cs，
--          讓 categories.name 的 UNIQUE 約束天生不分大小寫）
-- storage engine：InnoDB
--
-- 本版異動（上下架搬到變體層級，推翻先前「作用於商品層級」的決策）：
--   products 拿掉 status 欄位；product_variants 新增 status 欄位，
--   每個尺寸各自獨立上下架，不再綁定同商品其他尺寸。
-- ============================================================

CREATE DATABASE IF NOT EXISTS cake_ordering_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE cake_ordering_db;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS product_variants;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS admins;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- categories（商品分類）
-- ============================================================
CREATE TABLE categories (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50)  NOT NULL,
    sort_order  INT          NOT NULL DEFAULT 0,
    created_at  DATETIME     NOT NULL,

    PRIMARY KEY (id),

    -- uk_categories_name：分類名稱查重（FR-CAT-002/003 的 WHERE 比對）
    -- ＋ 資料庫層擋下應用層「先查再寫」的競態重複。
    -- collation 為 utf8mb4_general_ci，UNIQUE 天生不分大小寫。
    UNIQUE KEY uk_categories_name (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ============================================================
-- products（商品基本資料，含蛋糕與加購配件）
-- 不含 status：上下架改為變體層級（見 product_variants），
-- 商品是否還「有東西可賣」由查詢時判斷底下是否存在可購買的變體。
-- ============================================================
CREATE TABLE products (
    id           BIGINT         NOT NULL AUTO_INCREMENT,
    name         VARCHAR(100)   NOT NULL,
    category_id  BIGINT         NOT NULL,
    description  TEXT           NULL,
    image_url    VARCHAR(255)   NULL,
    is_deleted   TINYINT(1)     NOT NULL DEFAULT 0,
    created_at   DATETIME       NOT NULL,
    updated_at   DATETIME       NOT NULL,

    PRIMARY KEY (id),

    -- idx_products_category_id：JOIN categories 取分類名稱；
    -- FR-CAT-004 刪除分類前的引用檢查（WHERE category_id = ?，不分 is_deleted）；
    -- 列表依分類篩選。同時作為下方 FK 約束所需的索引。
    KEY idx_products_category_id (category_id),

    -- idx_products_list：前後台商品列表最常見的 WHERE 組合。
    -- 不含 status（已移至 product_variants，見該表 idx_variants_status_deleted）。
    KEY idx_products_list (is_deleted, category_id),

    -- products.category_id -> categories.id，ON DELETE RESTRICT：
    -- FR-CAT-004「分類刪除防呆」的資料庫層兜底機制。
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id) REFERENCES categories (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ============================================================
-- product_variants（商品變體，即各尺寸的價格、庫存與上下架狀態；防超賣核心資料表）
-- status 作用於變體層級：每個尺寸各自獨立上下架，不再綁定同商品其他尺寸；
-- 缺貨（stock = 0）跟主動下架（status = INACTIVE）是兩件不同的事，
-- 缺貨不會自動改變 status。
-- ============================================================
CREATE TABLE product_variants (
    id           BIGINT         NOT NULL AUTO_INCREMENT,
    product_id   BIGINT         NOT NULL,
    size         VARCHAR(20)    NULL,
    price        DECIMAL(10,2)  NOT NULL,
    stock        INT            NOT NULL DEFAULT 0,
    status       VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    version      INT            NOT NULL DEFAULT 0,
    is_deleted   TINYINT(1)     NOT NULL DEFAULT 0,
    created_at   DATETIME       NOT NULL,
    updated_at   DATETIME       NOT NULL,

    PRIMARY KEY (id),

    -- idx_variants_product_id：依商品查詢底下所有變體（列表頁展開、詳細頁載入
    -- 尺寸選單）；商品整組軟刪除時的 UPDATE ... WHERE product_id = ?。
    -- 同時作為下方 FK 約束所需的索引。
    KEY idx_variants_product_id (product_id),

    -- idx_variants_product_deleted：過濾出某商品「未刪除」的變體
    -- （後台顯示可選尺寸，含已下架）最常見組合。
    KEY idx_variants_product_deleted (product_id, is_deleted),

    -- idx_variants_status_deleted：前台商品列表／詳細查詢「可購買變體」
    -- 最常見的 WHERE 組合（is_deleted = 0 AND status = 'ACTIVE'），
    -- 本次隨上下架欄位搬遷新增。
    KEY idx_variants_status_deleted (is_deleted, status),

    -- product_variants.product_id -> products.id，一般外鍵，刻意不寫 ON DELETE
    -- 子句（ERD 4.2 節：商品／變體皆一律軟刪除，不會觸發 DELETE；商品整組軟刪除
    -- 時由應用層 UPDATE product_variants SET is_deleted = 1 WHERE product_id = ?
    -- 連動，非資料庫層 CASCADE）。
    CONSTRAINT fk_variants_product
        FOREIGN KEY (product_id) REFERENCES products (id)
        ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ============================================================
-- orders（訂單主檔）
-- ============================================================
CREATE TABLE orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_no VARCHAR(30) NOT NULL,
    customer_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    shipping_method VARCHAR(20) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    address VARCHAR(255) NULL,
    shipping_fee DECIMAL(10,2) NOT NULL,
    pickup_date DATE NOT NULL,
    remark TEXT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    -- uk_orders_order_no：訂單編號唯一性約束（撞號時的最後防線）；
    -- 訪客訂單查詢 WHERE order_no = ? AND phone = ? 先走此索引命中單筆再比對電話。
    UNIQUE KEY uk_orders_order_no (order_no),
    -- idx_orders_status_created_at：後台訂單列表 WHERE status = ? 篩選
    -- ＋ 依 created_at 排序／分頁的典型組合；不帶 status 篩選時排序亦可利用
    -- created_at 部分（複合索引最左前綴特性）。
    KEY idx_orders_status_created_at (status, created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- ============================================================
-- order_items（訂單品項）
-- ============================================================
CREATE TABLE order_items (
    id            BIGINT         NOT NULL AUTO_INCREMENT,
    order_id      BIGINT         NOT NULL,
    variant_id    BIGINT         NOT NULL,
    product_name  VARCHAR(100)   NOT NULL,
    variant_size  VARCHAR(20)    NULL,
    unit_price    DECIMAL(10,2)  NOT NULL,
    quantity      INT            NOT NULL,
    subtotal      DECIMAL(10,2)  NOT NULL,

    PRIMARY KEY (id),

    -- idx_order_items_order_id：查詢訂單明細／取消訂單回補庫存時
    -- WHERE order_id = ? 撈出所有品項（最高頻 JOIN）。同時作為下方 FK 約束所需的索引。
    KEY idx_order_items_order_id (order_id),

    -- idx_order_items_variant_id：外鍵完整性檢查用；MVP 無「依變體反查訂單」
    -- 的功能，保留即可。同時作為下方 FK 約束所需的索引。
    KEY idx_order_items_variant_id (variant_id),

    -- order_items.order_id -> orders.id，ON DELETE CASCADE：
    -- 刪除訂單主檔時一併刪除明細（MVP 不提供刪除訂單 API，僅供資料完整性保障）。
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    -- order_items.variant_id -> product_variants.id，一般外鍵，刻意不寫 ON DELETE
    -- 子句（ERD 4.3 節：變體一律軟刪除，不會觸發 DELETE，此外鍵僅保障不會插入
    -- 指向不存在變體的資料）。
    CONSTRAINT fk_order_items_variant
        FOREIGN KEY (variant_id) REFERENCES product_variants (id)
        ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ============================================================
-- admins（管理員）
-- 僅一列資料，由 .env 的 ADMIN_INITIAL_USERNAME／ADMIN_INITIAL_PASSWORD
-- 於後端啟動時（CommandLineRunner，密碼經 BCrypt 雜湊）建立，
-- 不可 fallback 到寫死密碼，故本檔不寫入任何帳號資料（保持空表）。
-- ============================================================
CREATE TABLE admins (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    username       VARCHAR(50)   NOT NULL,
    password_hash  VARCHAR(255)  NOT NULL,
    created_at     DATETIME      NOT NULL,

    PRIMARY KEY (id),

    -- uk_admins_username：登入時 WHERE username = ? 查詢＋帳號唯一性約束。
    UNIQUE KEY uk_admins_username (username)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ============================================================
-- 種子資料：categories 四筆（後端需求書 4.2／9.3 節、ERD 第 6 章）
-- ============================================================
INSERT INTO categories (name, sort_order, created_at) VALUES
    ('水果系列蛋糕', 1, NOW()),
    ('千層系列蛋糕', 2, NOW()),
    ('起司系列蛋糕', 3, NOW()),
    ('配件',        4, NOW());

-- ============================================================
-- admins：刻意不寫入任何資料，保持空表。
-- 帳號由後端程式啟動時依環境變數 ADMIN_INITIAL_USERNAME／
-- ADMIN_INITIAL_PASSWORD 建立（見後端需求書 2.6／12.2 節）。
-- ============================================================