-- Seed mock data for Suppliers
INSERT INTO suppliers (id, name, phone, email, is_active) VALUES
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Công ty Cổ phần Dược phẩm Vimedimex', '0243851523', 'contact@vimedimex.vn', true),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'Công ty TNHH Zoetis Việt Nam', '02839151515', 'info@zoetis.com', true),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'Công ty TNHH Bayer Việt Nam', '02838450828', 'animalhealth@bayer.com', true);

-- Seed mock data for Medicines
INSERT INTO medicines (id, name, sku, unit, min_stock, import_price, sell_price, is_active) VALUES
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Thuốc trị rận rận NexGard Spectra (2-3.5kg)', 'MED-NEX-001', 'Hộp', 10, 150000, 200000, true),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'Thuốc tẩy giun Drontal Plus', 'MED-DRO-001', 'Viên', 50, 25000, 35000, true),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'Thuốc kháng sinh Baytril 50mg', 'MED-BAY-001', 'Vỉ', 20, 80000, 120000, true),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'Vắc xin phòng bệnh dại Rabisin', 'MED-RAB-001', 'Liều', 30, 45000, 70000, true);

-- Seed mock data for Stock Batches
INSERT INTO stock_batches (id, medicine_id, supplier_id, batch_code, quantity, remaining_qty, import_price, expiry_date) VALUES
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'BATCH-NEX-202310', 100, 85, 150000, '2027-12-31'),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'BATCH-DRO-202311', 200, 150, 25000, '2028-06-30'),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'BATCH-BAY-202309', 50, 20, 80000, '2026-09-15');

-- Seed mock data for Stock Vouchers
INSERT INTO stock_vouchers (id, type, status, note, created_at, updated_at) VALUES
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'IMPORT', 'DRAFT', 'Phiếu nhập hàng quý 4/2023', '2023-10-01 08:00:00+07', '2023-10-01 08:30:00+07'),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'EXPORT', 'DRAFT', 'Xuất hàng sử dụng nội bộ phòng khám', '2023-11-05 14:00:00+07', '2023-11-05 14:15:00+07'),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'IMPORT', 'DRAFT', 'Phiếu nhập hàng quý 1/2024 (Đang chờ duyệt)', '2024-01-15 09:00:00+07', '2024-01-15 09:00:00+07');

-- Seed mock data for Stock Voucher Items
INSERT INTO stock_voucher_items (id, voucher_id, batch_id, medicine_id, quantity, unit_price, note) VALUES
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 100, 150000, 'Nhập đủ số lượng'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 200, 25000, 'Hàng cận date, cần lưu ý'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 15, 200000, 'Xuất cho ca khám bệnh ngoài giờ'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 50, 80000, 'Chờ duyệt');
