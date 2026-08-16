
-- ============================================================
-- V13: SHOP ORDERS (Online Shop)
-- Depends on: users, products
-- Separate from clinic invoices (V12)
-- ============================================================

-- 1. SHOP ORDERS
CREATE TABLE shop_orders (
    id                  UUID PRIMARY KEY,
    order_code          VARCHAR(50) NOT NULL,
    user_id             UUID,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    subtotal            NUMERIC(15,2) NOT NULL DEFAULT 0,
    shipping_fee        NUMERIC(15,2) NOT NULL DEFAULT 0,
    total_amount        NUMERIC(15,2) NOT NULL DEFAULT 0,
    payment_method      VARCHAR(30) NOT NULL DEFAULT 'COD',
    recipient_name      VARCHAR(150) NOT NULL,
    recipient_phone     VARCHAR(20) NOT NULL,
    shipping_address    VARCHAR(500) NOT NULL,
    note                VARCHAR(500),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_shop_orders_code
        UNIQUE (order_code),

    CONSTRAINT fk_shop_orders_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE SET NULL,

    CONSTRAINT ck_shop_orders_status
        CHECK (status IN ('PENDING', 'CONFIRMED', 'PREPARING', 'SHIPPING', 'COMPLETED', 'CANCELLED')),

    CONSTRAINT ck_shop_orders_subtotal
        CHECK (subtotal >= 0),

    CONSTRAINT ck_shop_orders_shipping_fee
        CHECK (shipping_fee >= 0),

    CONSTRAINT ck_shop_orders_total
        CHECK (total_amount >= 0),

    CONSTRAINT ck_shop_orders_payment_method
        CHECK (payment_method IN ('COD', 'CASH', 'CARD', 'BANK_TRANSFER', 'VNPAY', 'MOMO'))
);

CREATE INDEX ix_shop_orders_user_created
    ON shop_orders(user_id, created_at DESC)
    WHERE user_id IS NOT NULL;

CREATE INDEX ix_shop_orders_status_created
    ON shop_orders(status, created_at DESC);

-- 2. SHOP ORDER ITEMS
CREATE TABLE shop_order_items (
    id                  UUID PRIMARY KEY,
    order_id            UUID NOT NULL,
    product_id          UUID,
    product_name        VARCHAR(255) NOT NULL,
    product_image       VARCHAR(500),
    quantity            INTEGER NOT NULL,
    unit_price          NUMERIC(12,2) NOT NULL,
    total               NUMERIC(15,2) NOT NULL,

    CONSTRAINT fk_shop_order_items_order
        FOREIGN KEY (order_id)
        REFERENCES shop_orders(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_shop_order_items_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE SET NULL,

    CONSTRAINT ck_shop_order_items_quantity
        CHECK (quantity > 0),

    CONSTRAINT ck_shop_order_items_unit_price
        CHECK (unit_price >= 0),

    CONSTRAINT ck_shop_order_items_total
        CHECK (total >= 0)
);

CREATE INDEX ix_shop_order_items_order
    ON shop_order_items(order_id);

CREATE INDEX ix_shop_order_items_product
    ON shop_order_items(product_id)
    WHERE product_id IS NOT NULL;

-- 3. SEED: ROLE_SHOP_STAFF + test user
-- Insert role if not exists
INSERT INTO roles (id, name, description, created_at)
SELECT gen_random_uuid(), 'ROLE_SHOP_STAFF', 'Nhân viên cửa hàng', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_SHOP_STAFF');

-- Insert shop staff user (password: 123456 encoded with Argon2)
-- The hash below is for '123456' with Spring Security 5.8 Argon2 defaults
INSERT INTO users (id, username, password, email, full_name, enabled, phone, created_at, updated_at)
SELECT gen_random_uuid(), 'shopstaff', '$argon2id$v=19$m=16384,t=2,p=1$ZEtnNHlraHlqMWFlNHJqMg$mBuOXMGqfBpIbCFSHhsLN5mCxvZfkMJi9NGZY/wMrBo', 'shopstaff@vetimate.com', 'NV Shop VetiMate', true, '0901234567', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'shopstaff@vetimate.com');

-- Assign ROLE_SHOP_STAFF to the user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'shopstaff@vetimate.com' AND r.name = 'ROLE_SHOP_STAFF'
AND NOT EXISTS (
    SELECT 1 FROM user_roles ur
    WHERE ur.user_id = u.id AND ur.role_id = r.id
);
