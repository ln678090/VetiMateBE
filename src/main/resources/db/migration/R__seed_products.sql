-- V17: SEED PRODUCTS

-- We need to ensure we have a category and a brand first. We can use existing ones or create new ones.
-- To avoid foreign key errors, we will create 1 category and 1 brand if they don't exist, or just insert them with known UUIDs.
-- Wait, the simplest way is to fetch an existing category and brand ID using a subquery, or we can just create some with fixed UUIDs.

INSERT INTO categories (id, name, slug, parent_id, description, sort_order)
VALUES ('019058c1-1111-7a1a-b309-111111111111', 'Thức ăn cho chó', 'thuc-an-cho-cho', NULL, 'Thức ăn hạt, pate, thức ăn ướt cho chó', 1)
ON CONFLICT (slug) DO NOTHING;

INSERT INTO brands (id, name, slug, description)
VALUES ('019058c1-2222-7a1a-b309-222222222222', 'Royal Canin', 'royal-canin', 'Thương hiệu thức ăn thú cưng từ Pháp')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (id, name, slug, description, short_desc, category_id, brand_id, pet_type, price, stock_quantity, image_url, is_featured, is_active)
VALUES
('019058c1-3333-7a1a-b309-333333333333', 'Pate Royal Canin Poodle Adult 85g', 'pate-royal-canin-poodle-adult-85g', 'Pate dinh dưỡng đặc biệt cho giống chó Poodle.', 'Pate thơm ngon', (SELECT id FROM categories WHERE slug='thuc-an-cho-cho' LIMIT 1), (SELECT id FROM brands WHERE slug='royal-canin' LIMIT 1), 'dog', 35000, 10, 'https://images.unsplash.com/photo-1583337130417-3346a1be7dee?w=400&q=80', true, true),
('019058c1-4444-7a1a-b309-444444444444', 'Hạt Royal Canin Mini Puppy 2kg', 'hat-royal-canin-mini-puppy-2kg', 'Hạt khô cho chó con giống nhỏ dưới 10 tháng tuổi.', 'Hạt chó con', (SELECT id FROM categories WHERE slug='thuc-an-cho-cho' LIMIT 1), (SELECT id FROM brands WHERE slug='royal-canin' LIMIT 1), 'dog', 390000, 0, 'https://images.unsplash.com/photo-1568644396922-5c3bfae12521?w=400&q=80', true, true),
('019058c1-5555-7a1a-b309-555555555555', 'Xương gặm sạch răng Pedigree', 'xuong-gam-sach-rang-pedigree', 'Xương gặm giúp làm sạch răng và thơm miệng cho chó.', 'Xương gặm', (SELECT id FROM categories WHERE slug='thuc-an-cho-cho' LIMIT 1), (SELECT id FROM brands WHERE slug='royal-canin' LIMIT 1), 'dog', 45000, 3, 'https://images.unsplash.com/photo-1517849845537-4d257902454a?w=400&q=80', false, true)
ON CONFLICT (slug) DO UPDATE SET image_url = EXCLUDED.image_url;
