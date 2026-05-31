-- ===== Products =====
CREATE TABLE products (
    id              UUID PRIMARY KEY,                            -- Java UUIDv7
    name            VARCHAR(255) NOT NULL,
    slug            VARCHAR(255) NOT NULL UNIQUE,
    sku             VARCHAR(50) UNIQUE,
    description     TEXT,
    short_desc      VARCHAR(500),

    category_id     UUID NOT NULL,
    brand_id        UUID NOT NULL,
    pet_type        VARCHAR(10) NOT NULL DEFAULT 'both',         -- 'dog', 'cat', 'both'

    price           NUMERIC(12,2) NOT NULL,
    original_price  NUMERIC(12,2),
    stock_quantity  INT NOT NULL DEFAULT 0,

    rating          NUMERIC(2,1) NOT NULL DEFAULT 0,             -- 0 - 5
    review_count    INT NOT NULL DEFAULT 0,

    image_url       VARCHAR(500) NOT NULL,
    gallery_urls    TEXT,                                        -- JSON array stored as text

    is_featured     BOOLEAN NOT NULL DEFAULT FALSE,
    is_new          BOOLEAN NOT NULL DEFAULT FALSE,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,

    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_product_category
        FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    CONSTRAINT fk_product_brand
        FOREIGN KEY (brand_id) REFERENCES brands(id) ON DELETE RESTRICT,
    CONSTRAINT chk_product_pet_type
        CHECK (pet_type IN ('dog', 'cat', 'both')),
    CONSTRAINT chk_product_rating
        CHECK (rating >= 0 AND rating <= 5)
);

CREATE INDEX idx_products_category   ON products(category_id);
CREATE INDEX idx_products_brand      ON products(brand_id);
CREATE INDEX idx_products_pet_type   ON products(pet_type);
CREATE INDEX idx_products_active     ON products(is_active);
CREATE INDEX idx_products_featured   ON products(is_featured) WHERE is_featured = TRUE;
CREATE INDEX idx_products_slug       ON products(slug);
CREATE INDEX idx_products_price      ON products(price);

-- Full-text search index (Vietnamese - dùng simple cho compat)
CREATE INDEX idx_products_search ON products
    USING GIN (to_tsvector('simple', name || ' ' || COALESCE(description, '')));

-- ===== Seed 12 products (khớp với mock FE) =====
INSERT INTO products (id, name, slug, sku, description, short_desc, category_id, brand_id, pet_type, price, original_price, stock_quantity, rating, review_count, image_url, is_featured, is_new) VALUES
-- 1 Royal Canin Mini Adult
('550e8400-e29b-41d4-a716-446655440201',
 'Royal Canin Mini Adult 4kg', 'royal-canin-mini-adult', 'RC-MINI-4KG',
 'Thức ăn hạt cao cấp cho chó trưởng thành giống nhỏ. Công thức cân bằng dinh dưỡng với protein chất lượng cao, hỗ trợ hệ tiêu hóa nhạy cảm.',
 'Thức ăn hạt cao cấp cho chó trưởng thành giống nhỏ',
 '550e8400-e29b-41d4-a716-446655440101', -- food-dry sub
 '550e8400-e29b-41d4-a716-446655440020', -- Royal Canin
 'dog', 850000, 920000, 50, 4.8, 312,
 'https://images.unsplash.com/photo-1568640347023-a616a30bc3bd?w=600',
 TRUE, FALSE),

-- 2 Whiskas Tuna
('550e8400-e29b-41d4-a716-446655440202',
 'Whiskas Tuna 1kg', 'whiskas-tuna-1kg', 'WK-TUNA-1KG',
 'Hạt khô cho mèo trưởng thành vị cá ngừ. Bổ sung taurine cho mắt khoẻ.',
 'Hạt khô cho mèo trưởng thành vị cá ngừ',
 '550e8400-e29b-41d4-a716-446655440101',
 '550e8400-e29b-41d4-a716-446655440021',
 'cat', 145000, NULL, 80, 4.6, 198,
 'https://images.unsplash.com/photo-1583511655826-05700d52f4d9?w=600',
 FALSE, FALSE),

-- 3 Kong Classic
('550e8400-e29b-41d4-a716-446655440203',
 'Kong Classic Rubber Toy', 'kong-classic-rubber', 'KG-CLASSIC',
 'Đồ chơi cao su bền siêu nhai cho chó. Có thể nhồi thức ăn bên trong để pet bận rộn hàng giờ.',
 'Đồ chơi cao su bền siêu nhai cho chó',
 '550e8400-e29b-41d4-a716-446655440011',
 '550e8400-e29b-41d4-a716-446655440022',
 'dog', 320000, NULL, 30, 4.9, 421,
 'https://images.unsplash.com/photo-1601758228041-f3b2795255f1?w=600',
 TRUE, FALSE),

-- 4 Catit Flower Fountain
('550e8400-e29b-41d4-a716-446655440204',
 'Catit Flower Fountain 3L', 'catit-flower-fountain', 'CT-FLOWER-3L',
 'Đài phun nước cho mèo hình hoa. Lọc 3 lớp, khuyến khích mèo uống nhiều nước.',
 'Đài phun nước cho mèo hình hoa',
 '550e8400-e29b-41d4-a716-446655440013',
 '550e8400-e29b-41d4-a716-446655440023',
 'cat', 680000, 750000, 25, 4.7, 156,
 'https://images.unsplash.com/photo-1574144611937-0df059b5ef3e?w=600',
 FALSE, TRUE),

-- 5 Tofu Cat Litter
('550e8400-e29b-41d4-a716-446655440205',
 'Tofu Cat Litter 6L', 'tofu-cat-litter-6l', 'TF-LITTER-6L',
 'Cát đậu nành phân hủy sinh học, khử mùi tốt, an toàn nếu mèo lỡ ăn phải.',
 'Cát đậu nành phân hủy sinh học, khử mùi tốt',
 '550e8400-e29b-41d4-a716-446655440012',
 '550e8400-e29b-41d4-a716-446655440023',
 'cat', 195000, NULL, 60, 4.5, 89,
 'https://images.unsplash.com/photo-1585159812596-fac104f2f069?w=600',
 FALSE, FALSE),

-- 6 Collar set
('550e8400-e29b-41d4-a716-446655440206',
 'Bộ vòng cổ + dây dắt cao cấp', 'feeding-collar-set', 'PK-COLLAR-SET',
 'Dây dắt da PU êm tay, có khóa an toàn, vòng cổ chỉnh size linh hoạt.',
 'Dây dắt da PU êm tay, có khóa an toàn',
 '550e8400-e29b-41d4-a716-446655440013',
 '550e8400-e29b-41d4-a716-446655440023',
 'both', 410000, NULL, 0, 4.4, 67,
 'https://images.unsplash.com/photo-1601758003122-53c40e686a19?w=600',
 FALSE, FALSE),

-- 7 Pedigree Puppy
('550e8400-e29b-41d4-a716-446655440207',
 'Pedigree Puppy 3kg', 'pedigree-puppy-3kg', 'PG-PUPPY-3KG',
 'Thức ăn cho chó con phát triển toàn diện, giàu DHA cho não bộ.',
 'Thức ăn cho chó con phát triển toàn diện',
 '550e8400-e29b-41d4-a716-446655440101',
 '550e8400-e29b-41d4-a716-446655440024',
 'dog', 285000, NULL, 100, 4.3, 234,
 'https://images.unsplash.com/photo-1589924691995-400dc9ecc119?w=600',
 FALSE, FALSE),

-- 8 Ball rope toy
('550e8400-e29b-41d4-a716-446655440208',
 'Bóng bện dây thừng', 'ball-rope-toy', 'TY-BALL-ROPE',
 'Đồ chơi bóng dây thừng kéo co, tốt cho răng nướu pet.',
 'Đồ chơi bóng dây thừng kéo co',
 '550e8400-e29b-41d4-a716-446655440011',
 '550e8400-e29b-41d4-a716-446655440023',
 'both', 65000, NULL, 200, 4.2, 145,
 'https://images.unsplash.com/photo-1546975490-e8b92a360b24?w=600',
 FALSE, FALSE),

-- 9 Bentonite litter
('550e8400-e29b-41d4-a716-446655440209',
 'Cát Bentonite 10L', 'bentonite-litter-10l', 'BT-LITTER-10L',
 'Cát đất sét vón cục nhanh, ít bụi, hương lavender dịu nhẹ.',
 'Cát đất sét vón cục nhanh, ít bụi',
 '550e8400-e29b-41d4-a716-446655440012',
 '550e8400-e29b-41d4-a716-446655440023',
 'cat', 165000, NULL, 90, 4.6, 278,
 'https://images.unsplash.com/photo-1592194996308-7b43878e84a6?w=600',
 TRUE, FALSE),

-- 10 Pet Shampoo
('550e8400-e29b-41d4-a716-446655440210',
 'Sữa tắm chuyên dụng 500ml', 'premium-pet-shampoo', 'SP-SHAMPOO-500',
 'Sữa tắm dưỡng lông, không kích ứng da, pH cân bằng cho thú cưng.',
 'Sữa tắm dưỡng lông, không kích ứng da',
 '550e8400-e29b-41d4-a716-446655440014',
 '550e8400-e29b-41d4-a716-446655440023',
 'both', 230000, NULL, 45, 4.7, 92,
 'https://images.unsplash.com/photo-1535930891776-0c2dfb7fda1a?w=600',
 FALSE, TRUE),

-- 11 Smart Feeder
('550e8400-e29b-41d4-a716-446655440211',
 'Máy cho ăn tự động 4L', 'smart-feeder-4l', 'SM-FEEDER-4L',
 'Máy cho ăn thông minh kết nối WiFi qua app. Lên lịch chính xác từng bữa.',
 'Máy cho ăn thông minh kết nối WiFi qua app',
 '550e8400-e29b-41d4-a716-446655440013',
 '550e8400-e29b-41d4-a716-446655440023',
 'both', 1850000, 2100000, 15, 4.8, 134,
 'https://images.unsplash.com/photo-1623387641168-d9803ddd3f35?w=600',
 TRUE, FALSE),

-- 12 Scratching post
('550e8400-e29b-41d4-a716-446655440212',
 'Cây cào móng cao 90cm', 'scratching-post-tall', 'SC-POST-90',
 'Trụ cào móng dây thừng bền cho mèo, có chỗ nghỉ trên đỉnh.',
 'Trụ cào móng dây thừng bền cho mèo',
 '550e8400-e29b-41d4-a716-446655440013',
 '550e8400-e29b-41d4-a716-446655440023',
 'cat', 520000, NULL, 20, 4.5, 76,
 'https://images.unsplash.com/photo-1606214174585-fe31582dc6ee?w=600',
 FALSE, FALSE);