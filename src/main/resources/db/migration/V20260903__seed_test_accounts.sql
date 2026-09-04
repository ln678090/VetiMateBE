-- V20260903__seed_test_accounts.sql
-- Seed tất cả tài khoản test với password: 123456
-- Argon2 hash: $argon2id$v=19$m=16384,t=2,p=1$mxPTpnoM4U+UAxSAAPiPFQ$biEj6BlWjxIps1rWAhFr0c+O4TY9YcrKxLjPL28kFZI

-- ============================================================
-- 1. TẠO CÁC TÀI KHOẢN USERS
-- ============================================================

INSERT INTO users (id, username, password, email, full_name, phone, enabled, created_at, updated_at, login_fail_count, mfa_enabled)
VALUES
    -- user12@gmail.com (Khách hàng)
    ('a0000000-0000-0000-0000-000000000001', 'user12', '$argon2id$v=19$m=16384,t=2,p=1$mxPTpnoM4U+UAxSAAPiPFQ$biEj6BlWjxIps1rWAhFr0c+O4TY9YcrKxLjPL28kFZI', 'user12@gmail.com', 'Người dùng 12', '0900000001', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),

    -- admin@gmail.com (Quản trị viên)
    ('a0000000-0000-0000-0000-000000000002', 'admin', '$argon2id$v=19$m=16384,t=2,p=1$mxPTpnoM4U+UAxSAAPiPFQ$biEj6BlWjxIps1rWAhFr0c+O4TY9YcrKxLjPL28kFZI', 'admin@gmail.com', 'Quản trị viên', '0900000002', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),

    -- receptionist@gmail.com (Lễ tân)
    ('a0000000-0000-0000-0000-000000000003', 'receptionist', '$argon2id$v=19$m=16384,t=2,p=1$mxPTpnoM4U+UAxSAAPiPFQ$biEj6BlWjxIps1rWAhFr0c+O4TY9YcrKxLjPL28kFZI', 'receptionist@gmail.com', 'Lễ tân', '0900000003', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),

    -- customer@gmail.com (Khách hàng)
    ('a0000000-0000-0000-0000-000000000004', 'customer', '$argon2id$v=19$m=16384,t=2,p=1$mxPTpnoM4U+UAxSAAPiPFQ$biEj6BlWjxIps1rWAhFr0c+O4TY9YcrKxLjPL28kFZI', 'customer@gmail.com', 'Khách hàng', '0900000004', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),

    -- manager@gmail.com (Quản lý)
    ('a0000000-0000-0000-0000-000000000005', 'manager', '$argon2id$v=19$m=16384,t=2,p=1$mxPTpnoM4U+UAxSAAPiPFQ$biEj6BlWjxIps1rWAhFr0c+O4TY9YcrKxLjPL28kFZI', 'manager@gmail.com', 'Quản lý', '0900000005', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),

    -- doctor1@gmail.com (Bác sĩ)
    ('a0000000-0000-0000-0000-000000000006', 'doctor1', '$argon2id$v=19$m=16384,t=2,p=1$mxPTpnoM4U+UAxSAAPiPFQ$biEj6BlWjxIps1rWAhFr0c+O4TY9YcrKxLjPL28kFZI', 'doctor1@gmail.com', 'Bác sĩ Nguyễn', '0900000006', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),

    -- accountant@gmail.com (Kế toán)
    ('a0000000-0000-0000-0000-000000000007', 'accountant', '$argon2id$v=19$m=16384,t=2,p=1$mxPTpnoM4U+UAxSAAPiPFQ$biEj6BlWjxIps1rWAhFr0c+O4TY9YcrKxLjPL28kFZI', 'accountant@gmail.com', 'Kế toán', '0900000007', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),

    -- warehouse@gmail.com (Thủ kho)
    ('a0000000-0000-0000-0000-000000000008', 'warehouse', '$argon2id$v=19$m=16384,t=2,p=1$mxPTpnoM4U+UAxSAAPiPFQ$biEj6BlWjxIps1rWAhFr0c+O4TY9YcrKxLjPL28kFZI', 'warehouse@gmail.com', 'Thủ kho', '0900000008', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),

    -- shopstaff@gmail.com (NV Shop)
    ('a0000000-0000-0000-0000-000000000009', 'shopstaff', '$argon2id$v=19$m=16384,t=2,p=1$mxPTpnoM4U+UAxSAAPiPFQ$biEj6BlWjxIps1rWAhFr0c+O4TY9YcrKxLjPL28kFZI', 'shopstaff@gmail.com', 'Nhân viên Shop', '0900000009', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),

    -- systempartner@gmail.com (Đối tác hệ thống)
    ('a0000000-0000-0000-0000-000000000010', 'systempartner', '$argon2id$v=19$m=16384,t=2,p=1$mxPTpnoM4U+UAxSAAPiPFQ$biEj6BlWjxIps1rWAhFr0c+O4TY9YcrKxLjPL28kFZI', 'systempartner@gmail.com', 'Đối tác hệ thống', '0900000010', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),

    -- hihi1@gmail.com (Khách hàng)
    ('a0000000-0000-0000-0000-000000000011', 'hihi1', '$argon2id$v=19$m=16384,t=2,p=1$mxPTpnoM4U+UAxSAAPiPFQ$biEj6BlWjxIps1rWAhFr0c+O4TY9YcrKxLjPL28kFZI', 'hihi1@gmail.com', 'Hihi', '0900000011', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false)

ON CONFLICT (username) DO NOTHING;

-- ============================================================
-- 2. GÁN ROLE CHO TỪNG TÀI KHOẢN
-- ============================================================
-- Role IDs (từ V1 và V13):
--   ROLE_USER:           550e8400-e29b-41d4-a716-446655440000
--   ROLE_ADMIN:          550e8400-e29b-41d4-a716-446655440001
--   ROLE_MANAGER:        550e8400-e29b-41d4-a716-446655440002
--   ROLE_DOCTOR:         550e8400-e29b-41d4-a716-446655440003
--   ROLE_RECEPTIONIST:   550e8400-e29b-41d4-a716-446655440004
--   ROLE_ACCOUNTANT:     550e8400-e29b-41d4-a716-446655440005
--   ROLE_WAREHOUSE:      550e8400-e29b-41d4-a716-446655440006
--   ROLE_SHOP_STAFF:     550e8400-e29b-41d4-a716-446655440007
--   ROLE_SYSTEM_PARTNER: 550e8400-e29b-41d4-a716-446655440008

INSERT INTO user_roles (user_id, role_id)
VALUES
    -- user12 -> ROLE_USER
    ('a0000000-0000-0000-0000-000000000001', '550e8400-e29b-41d4-a716-446655440000'),

    -- admin -> ROLE_ADMIN
    ('a0000000-0000-0000-0000-000000000002', '550e8400-e29b-41d4-a716-446655440001'),

    -- receptionist -> ROLE_RECEPTIONIST
    ('a0000000-0000-0000-0000-000000000003', '550e8400-e29b-41d4-a716-446655440004'),

    -- customer -> ROLE_USER
    ('a0000000-0000-0000-0000-000000000004', '550e8400-e29b-41d4-a716-446655440000'),

    -- manager -> ROLE_MANAGER
    ('a0000000-0000-0000-0000-000000000005', '550e8400-e29b-41d4-a716-446655440002'),

    -- doctor1 -> ROLE_DOCTOR
    ('a0000000-0000-0000-0000-000000000006', '550e8400-e29b-41d4-a716-446655440003'),

    -- accountant -> ROLE_ACCOUNTANT
    ('a0000000-0000-0000-0000-000000000007', '550e8400-e29b-41d4-a716-446655440005'),

    -- warehouse -> ROLE_WAREHOUSE
    ('a0000000-0000-0000-0000-000000000008', '550e8400-e29b-41d4-a716-446655440006'),

    -- shopstaff -> ROLE_SHOP_STAFF
    ('a0000000-0000-0000-0000-000000000009', '550e8400-e29b-41d4-a716-446655440007'),

    -- systempartner -> ROLE_SYSTEM_PARTNER
    ('a0000000-0000-0000-0000-000000000010', '550e8400-e29b-41d4-a716-446655440008'),

    -- hihi1 -> ROLE_USER
    ('a0000000-0000-0000-0000-000000000011', '550e8400-e29b-41d4-a716-446655440000')

ON CONFLICT (user_id, role_id) DO NOTHING;
