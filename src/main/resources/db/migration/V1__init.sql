-- ============================================================
-- 線上蛋糕訂購系統（WishCake）｜V1__init.sql（Flyway migration）
-- 依「後端需求書」4.2 節資料表設計、9.3 節種子資料、12.2 節 admin seed 備忘
-- 依「ERD」第 3 章欄位清單、第 4 章外鍵規則、第 5 章索引設計建議、第 6 章種子資料
-- 依「Claude Instructions」命名規範／樂觀鎖／軟刪除／charset 規則
--
-- charset：utf8mb4；collation：utf8mb4_general_ci（不分大小寫，非 _bin/_cs，
--          讓 categories.name 的 UNIQUE 約束天生不分大小寫）
-- storage engine：InnoDB
-- ============================================================

-- ============================================================
-- categories（商品分類）
-- ============================================================
CREATE TABLE categories (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50)  NOT NULL,
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
-- products（商品，含蛋糕與加購配件）
-- ============================================================
CREATE TABLE products (
    id           BIGINT         NOT NULL AUTO_INCREMENT,
    name         VARCHAR(100)   NOT NULL,
    size         VARCHAR(20)    NULL,
    category_id  BIGINT         NOT NULL,
    price        DECIMAL(10,2)  NOT NULL,
    stock        INT            NOT NULL DEFAULT 0,
    description  TEXT           NULL,
    image_url    VARCHAR(255)   NULL,
    status       VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    is_deleted   TINYINT(1)     NOT NULL DEFAULT 0,
    version      INT            NOT NULL DEFAULT 0,
    created_at   DATETIME       NOT NULL,
    updated_at   DATETIME       NOT NULL,

    PRIMARY KEY (id),

    KEY idx_products_category_id (category_id),

    KEY idx_products_list (is_deleted, status, category_id),

    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id) REFERENCES categories (id)
        ON DELETE RESTRICT
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
    UNIQUE KEY uk_orders_order_no (order_no),
    KEY idx_orders_status_created_at (status, created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- ============================================================
-- order_items（訂單品項）
-- ============================================================
CREATE TABLE order_items (
    id            BIGINT         NOT NULL AUTO_INCREMENT,
    order_id      BIGINT         NOT NULL,
    product_id    BIGINT         NOT NULL,
    product_name  VARCHAR(100)   NOT NULL,
    unit_price    DECIMAL(10,2)  NOT NULL,
    quantity      INT            NOT NULL,
    subtotal      DECIMAL(10,2)  NOT NULL,

    PRIMARY KEY (id),

    KEY idx_order_items_order_id (order_id),

    KEY idx_order_items_product_id (product_id),

    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id) REFERENCES products (id)
        ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ============================================================
-- admins（管理員）
-- ============================================================
CREATE TABLE admins (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    username       VARCHAR(50)   NOT NULL,
    password_hash  VARCHAR(255)  NOT NULL,
    created_at     DATETIME      NOT NULL,

    PRIMARY KEY (id),

    UNIQUE KEY uk_admins_username (username)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ============================================================
-- 種子資料：categories 四筆
-- ============================================================
INSERT INTO categories (name, created_at) VALUES
    ('水果系列蛋糕', NOW()),
    ('千層系列蛋糕', NOW()),
    ('起司系列蛋糕', NOW()),
    ('配件',        NOW());

-- ============================================================
-- admins：刻意不寫入任何資料，保持空表。
-- ============================================================
