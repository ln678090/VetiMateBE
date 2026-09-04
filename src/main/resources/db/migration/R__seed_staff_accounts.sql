-- R__seed_staff_accounts.sql
-- Repeatable migration to seed test staff accounts (idempotent)
-- Mỗi tài khoản có 1 role riêng biệt
-- Password: 123456 (Argon2 hash)

-- ============================================================
-- 1. LỄ TÂN (RECEPTIONIST)
-- ============================================================
INSERT INTO users (
    id, username, password, email, full_name, phone, enabled, created_at, updated_at, login_fail_count, mfa_enabled
)
VALUES (
    'b70c3600-e29b-41d4-a716-446655440001',
    'staff01',
    '$argon2id$v=19$m=16384,t=2,p=1$mxPTpnoM4U+UAxSAAPiPFQ$biEj6BlWjxIps1rWAhFr0c+O4TY9YcrKxLjPL28kFZI',
    'staff01@vetimate.vn',
    'Lễ tân Minh',
    '0901000001',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0,
    false
)
ON CONFLICT (username) DO UPDATE SET
    full_name = EXCLUDED.full_name,
    password = EXCLUDED.password,
    phone = EXCLUDED.phone;

-- Xóa role cũ (gỡ bỏ 3 role gộp) rồi gán lại đúng 1 role
DELETE FROM user_roles WHERE user_id = 'b70c3600-e29b-41d4-a716-446655440001';
INSERT INTO user_roles (user_id, role_id)
VALUES
    ('b70c3600-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440004')
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO staff (
    id, user_id, full_name, phone, role_type, base_salary, commission_rate, is_active, created_at, updated_at
)
VALUES (
    'b70c3600-e29b-41d4-a716-446655440002',
    'b70c3600-e29b-41d4-a716-446655440001',
    'Lễ tân Minh',
    '0901000001',
    'RECEPTIONIST',
    8000000.00,
    0.00,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (user_id) DO UPDATE SET
    full_name = EXCLUDED.full_name,
    role_type = EXCLUDED.role_type;

-- ============================================================
-- 2. KẾ TOÁN (ACCOUNTANT)
-- ============================================================
INSERT INTO users (
    id, username, password, email, full_name, phone, enabled, created_at, updated_at, login_fail_count, mfa_enabled
)
VALUES (
    'b70c3600-e29b-41d4-a716-446655440003',
    'accountant01',
    '$argon2id$v=19$m=16384,t=2,p=1$mxPTpnoM4U+UAxSAAPiPFQ$biEj6BlWjxIps1rWAhFr0c+O4TY9YcrKxLjPL28kFZI',
    'accountant01@vetimate.vn',
    'Kế toán Hoa',
    '0901000002',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0,
    false
)
ON CONFLICT (username) DO UPDATE SET
    full_name = EXCLUDED.full_name,
    password = EXCLUDED.password,
    phone = EXCLUDED.phone;

INSERT INTO user_roles (user_id, role_id)
VALUES
    ('b70c3600-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440005')
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO staff (
    id, user_id, full_name, phone, role_type, base_salary, commission_rate, is_active, created_at, updated_at
)
VALUES (
    'b70c3600-e29b-41d4-a716-446655440004',
    'b70c3600-e29b-41d4-a716-446655440003',
    'Kế toán Hoa',
    '0901000002',
    'ACCOUNTANT',
    9000000.00,
    0.00,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (user_id) DO UPDATE SET
    full_name = EXCLUDED.full_name,
    role_type = EXCLUDED.role_type;

-- ============================================================
-- 3. NHÂN VIÊN SHOP (SHOP_STAFF)
-- ============================================================
INSERT INTO users (
    id, username, password, email, full_name, phone, enabled, created_at, updated_at, login_fail_count, mfa_enabled
)
VALUES (
    'b70c3600-e29b-41d4-a716-446655440005',
    'shopstaff01',
    '$argon2id$v=19$m=16384,t=2,p=1$mxPTpnoM4U+UAxSAAPiPFQ$biEj6BlWjxIps1rWAhFr0c+O4TY9YcrKxLjPL28kFZI',
    'shopstaff01@vetimate.vn',
    'NV Shop Tuấn',
    '0901000003',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0,
    false
)
ON CONFLICT (username) DO UPDATE SET
    full_name = EXCLUDED.full_name,
    password = EXCLUDED.password,
    phone = EXCLUDED.phone;

INSERT INTO user_roles (user_id, role_id)
VALUES
    ('b70c3600-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440007')
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO staff (
    id, user_id, full_name, phone, role_type, base_salary, commission_rate, is_active, created_at, updated_at
)
VALUES (
    'b70c3600-e29b-41d4-a716-446655440006',
    'b70c3600-e29b-41d4-a716-446655440005',
    'NV Shop Tuấn',
    '0901000003',
    'SHOP_STAFF',
    7500000.00,
    0.02,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (user_id) DO UPDATE SET
    full_name = EXCLUDED.full_name,
    role_type = EXCLUDED.role_type;
