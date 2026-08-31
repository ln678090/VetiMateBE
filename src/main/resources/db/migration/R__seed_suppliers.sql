-- V16: SEED SUPPLIERS

INSERT INTO suppliers (id, name, phone, email) VALUES
('019058b8-6ccb-7c0a-a309-883eb9e4f101', 'Nhà phân phối Royal Canin', '0901234567', 'contact@royalcanin.vn'),
('019058b8-7cda-7a1a-b309-994eb9e4f102', 'Công ty TNHH PetCity', '0912345678', 'info@petcity.vn')
ON CONFLICT (id) DO NOTHING;
