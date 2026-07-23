INSERT INTO clinic_services (id, name, description, price, duration_min, is_active, created_at, updated_at)
VALUES
    ('01890000-0000-7000-8000-000000000001', 'Khám tổng quát', 'Khám sức khỏe tổng quát cho thú cưng', 150000, 30, TRUE, now(), now()),
    ('01890000-0000-7000-8000-000000000002', 'Tiêm phòng dại', 'Tiêm vắc-xin phòng bệnh dại', 250000, 20, TRUE, now(), now()),
    ('01890000-0000-7000-8000-000000000003', 'Tiêm phòng 7 bệnh', 'Vắc-xin tổng hợp 7 bệnh cho chó', 350000, 20, TRUE, now(), now()),
    ('01890000-0000-7000-8000-000000000004', 'Tẩy giun', 'Tẩy giun sán định kỳ', 100000, 15, TRUE, now(), now()),
    ('01890000-0000-7000-8000-000000000005', 'Cắt tỉa lông - vệ sinh', 'Grooming: cắt tỉa lông, tắm, vệ sinh tai', 300000, 60, TRUE, now(), now()),
    ('01890000-0000-7000-8000-000000000006', 'Siêu âm chẩn đoán', 'Siêu âm ổ bụng chẩn đoán bệnh lý', 400000, 40, TRUE, now(), now()),
    ('01890000-0000-7000-8000-000000000007', 'Xét nghiệm máu', 'Xét nghiệm công thức máu tổng quát', 350000, 30, TRUE, now(), now()),
    ('01890000-0000-7000-8000-000000000008', 'Triệt sản (đực)', 'Phẫu thuật triệt sản thú cưng đực', 800000, 90, TRUE, now(), now()),
    ('01890000-0000-7000-8000-000000000009', 'Triệt sản (cái)', 'Phẫu thuật triệt sản thú cưng cái', 1200000, 120, TRUE, now(), now()),
    ('01890000-0000-7000-8000-000000000010', 'Khám cấp cứu', 'Khám cấp cứu ngoài giờ', 500000, 45, FALSE, now(), now())
ON CONFLICT (id) DO NOTHING;
