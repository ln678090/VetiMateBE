-- ===== Categories =====
CREATE TABLE categories (
    id          UUID PRIMARY KEY,                                -- Java UUIDv7 generate
    name        VARCHAR(100) NOT NULL,
    slug        VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(500),
    icon        VARCHAR(50),                                     -- lucide icon name
    parent_id   UUID,
    sort_order  INT NOT NULL DEFAULT 0,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_category_parent
        FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
);

CREATE INDEX idx_categories_parent  ON categories(parent_id);
CREATE INDEX idx_categories_slug    ON categories(slug);
CREATE INDEX idx_categories_active  ON categories(is_active);

-- ===== Brands =====
CREATE TABLE brands (
    id          UUID PRIMARY KEY,                                -- Java UUIDv7
    name        VARCHAR(100) NOT NULL,
    slug        VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(500),
    logo_url    VARCHAR(500),
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_brands_slug   ON brands(slug);
CREATE INDEX idx_brands_active ON brands(is_active);

-- ===== Seed 5 root categories =====
INSERT INTO categories (id, name, slug, description, icon, sort_order) VALUES
('550e8400-e29b-41d4-a716-446655440010', 'Thức ăn',        'food',         'Thức ăn hạt khô, pate, thức ăn ướt cho chó mèo', 'UtensilsCrossed', 1),
('550e8400-e29b-41d4-a716-446655440011', 'Đồ chơi',        'toys',         'Đồ chơi giải trí, vận động cho thú cưng',         'Bone',            2),
('550e8400-e29b-41d4-a716-446655440012', 'Cát vệ sinh',    'litter',       'Cát đậu nành, bentonite, cát silica',              'Sparkles',        3),
('550e8400-e29b-41d4-a716-446655440013', 'Phụ kiện',       'accessories',  'Vòng cổ, dây dắt, balo, lồng, máy cho ăn',         'Backpack',        4),
('550e8400-e29b-41d4-a716-446655440014', 'Spa & Chăm sóc', 'grooming',     'Sữa tắm, lược chải, cắt móng, dầu dưỡng',          'Bath',            5);

-- ===== Seed 5 sub-categories (Food children) =====
INSERT INTO categories (id, name, slug, parent_id, sort_order) VALUES
('550e8400-e29b-41d4-a716-446655440101', 'Hạt khô',  'food-dry',    '550e8400-e29b-41d4-a716-446655440010', 1),
('550e8400-e29b-41d4-a716-446655440102', 'Pate',     'food-pate',   '550e8400-e29b-41d4-a716-446655440010', 2),
('550e8400-e29b-41d4-a716-446655440103', 'Snack',    'food-snack',  '550e8400-e29b-41d4-a716-446655440010', 3);

-- ===== Seed 5 brands =====
INSERT INTO brands (id, name, slug, description) VALUES
('550e8400-e29b-41d4-a716-446655440020', 'Royal Canin', 'royal-canin', 'Thương hiệu thức ăn cao cấp Pháp'),
('550e8400-e29b-41d4-a716-446655440021', 'Whiskas',     'whiskas',     'Thức ăn cho mèo phổ biến nhất thế giới'),
('550e8400-e29b-41d4-a716-446655440022', 'Kong',        'kong',        'Đồ chơi cao su bền cho chó'),
('550e8400-e29b-41d4-a716-446655440023', 'Catit',       'catit',       'Phụ kiện thông minh cho mèo'),
('550e8400-e29b-41d4-a716-446655440024', 'Pedigree',    'pedigree',    'Thức ăn cho chó toàn diện');