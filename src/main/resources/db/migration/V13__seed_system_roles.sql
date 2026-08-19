
INSERT INTO roles (id, name, description, created_at)
VALUES
    (
        '550e8400-e29b-41d4-a716-446655440002',
        'ROLE_MANAGER',
        'Quản lý cửa hàng/phòng khám',
        CURRENT_TIMESTAMP
    ),
    (
        '550e8400-e29b-41d4-a716-446655440003',
        'ROLE_DOCTOR',
        'Bác sĩ thú y',
        CURRENT_TIMESTAMP
    ),
    (
        '550e8400-e29b-41d4-a716-446655440004',
        'ROLE_RECEPTIONIST',
        'Lễ tân và điều phối lịch khám',
        CURRENT_TIMESTAMP
    ),
    (
        '550e8400-e29b-41d4-a716-446655440005',
        'ROLE_ACCOUNTANT',
        'Kế toán',
        CURRENT_TIMESTAMP
    ),
    (
        '550e8400-e29b-41d4-a716-446655440006',
        'ROLE_WAREHOUSE',
        'Thủ kho',
        CURRENT_TIMESTAMP
    ),
    (
        '550e8400-e29b-41d4-a716-446655440007',
        'ROLE_SHOP_STAFF',
        'Nhân viên Shop',
        CURRENT_TIMESTAMP
    ),
    (
        '550e8400-e29b-41d4-a716-446655440008',
        'ROLE_SYSTEM_PARTNER',
        'Hệ thống hoặc đối tác tích hợp',
        CURRENT_TIMESTAMP
    )
ON CONFLICT (name) DO NOTHING;
