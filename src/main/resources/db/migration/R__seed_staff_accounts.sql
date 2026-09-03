-- R__seed_staff_accounts.sql
-- Repeatable migration to seed test staff account (idempotent)
-- Includes Argon2 hash for password: Staff@123

-- Create combined staff user if not exists
INSERT INTO users (
    id, username, password, email, full_name, phone, enabled, created_at, updated_at, login_fail_count, mfa_enabled
)
VALUES (
    'b70c3600-e29b-41d4-a716-446655440001',
    'staff01',
    '$argon2id$v=19$m=16384,t=2,p=1$nG7O8KGuE/xAq9ednIK9KQ$FgTiJl+omK300djiaQJDHybY/59pkqhjnbO1sFfd2Oo',
    'staff01@vetimate.vn',
    'Nhân viên Tổng hợp',
    '0901000999',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0,
    false
)
ON CONFLICT (username) DO NOTHING;

-- Map roles: RECEPTIONIST, ACCOUNTANT, SHOP_STAFF
INSERT INTO user_roles (user_id, role_id)
VALUES
    ('b70c3600-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440004'),
    ('b70c3600-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440005'),
    ('b70c3600-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440007')
ON CONFLICT (user_id, role_id) DO NOTHING;

-- Create staff record
INSERT INTO staff (
    id, user_id, full_name, phone, role_type, base_salary, commission_rate, is_active, created_at, updated_at
)
VALUES (
    'b70c3600-e29b-41d4-a716-446655440002',
    'b70c3600-e29b-41d4-a716-446655440001',
    'Nhân viên Tổng hợp',
    '0901000999',
    'RECEPTIONIST',
    8000000.00,
    0.00,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (user_id) DO NOTHING;
