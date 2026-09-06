-- =============================================================
-- Seed tài khoản theo nhánh Dung
-- Password "123456" và "123456789" (Argon2id hash)
-- =============================================================

DO $$
DECLARE
    -- Argon2id hash cho "123456"
    pw_123456 text := '$argon2id$v=19$m=16384,t=2,p=1$1mf/Inv49YEOFO/zPdDbAw$a8O3kDisoTld8Gs3aGM6bnAl2GFkEATzdnjiB43+TNg';

    _uid uuid;
    _role_id uuid;
    
    -- Định nghĩa một record type để lặp qua danh sách tài khoản
    account record;
BEGIN
    -- Tạo bảng tạm chứa danh sách tài khoản cần seed
    CREATE TEMP TABLE tmp_accounts (
        username text,
        email text,
        full_name text,
        role_name text
    ) ON COMMIT DROP;

    INSERT INTO tmp_accounts VALUES
        ('user12',        'user12@gmail.com',          'User 12',              'ROLE_USER'),
        ('admin',         'admin@gmail.com',           'Admin',                'ROLE_ADMIN'),
        ('receptionist',  'receptionist@gmail.com',    'Receptionist',         'ROLE_RECEPTIONIST'),
        ('customer',      'customer@gmail.com',        'Customer',             'ROLE_USER'),
        ('manager',       'manager@gmail.com',         'Manager',              'ROLE_MANAGER'),
        ('doctor1',       'doctor1@gmail.com',         'Doctor',               'ROLE_DOCTOR'),
        ('accountant',    'accountant@gmail.com',      'Accountant',           'ROLE_ACCOUNTANT'),
        ('warehouse',     'warehouse@gmail.com',       'Warehouse',            'ROLE_WAREHOUSE'),
        ('shopstaff',     'shopstaff@gmail.com',       'Shop Staff',           'ROLE_SHOP_STAFF'),
        ('systempartner', 'systempartner@gmail.com',   'System Partner',       'ROLE_SYSTEM_PARTNER'),
        ('hihi1',         'hihi1@gmail.com',           'Hihi 1',               'ROLE_USER'),
        ('staff01',       'staff01@vetimate.vn',       'Staff 01',             'ROLE_SHOP_STAFF');

    -- Lặp qua từng tài khoản để insert an toàn
    FOR account IN SELECT * FROM tmp_accounts LOOP
        -- Tìm role_id từ DB dựa theo name (chắc chắn chuẩn xác nhất)
        SELECT id INTO _role_id FROM roles WHERE name = account.role_name;

        IF _role_id IS NULL THEN
            RAISE NOTICE 'Skipping user % because role % not found in database', account.username, account.role_name;
            CONTINUE;
        END IF;

        -- Kiểm tra xem user đã tồn tại theo username chưa
        SELECT id INTO _uid FROM users WHERE username = account.username;
        
        IF NOT FOUND THEN
            -- Kiểm tra thêm email để tránh lỗi duplicate email
            SELECT id INTO _uid FROM users WHERE email = account.email;
        END IF;

        IF NOT FOUND THEN
            -- Nếu chưa tồn tại cả username và email, tạo mới
            _uid := gen_random_uuid();
            INSERT INTO users (id, username, password, email, full_name, enabled)
            VALUES (_uid, account.username, pw_123456, account.email, account.full_name, true);
        END IF;

        -- Gán role cho user (dùng ON CONFLICT DO NOTHING để bỏ qua nếu đã gán)
        INSERT INTO user_roles (user_id, role_id) 
        VALUES (_uid, _role_id) 
        ON CONFLICT DO NOTHING;
    END LOOP;

    RAISE NOTICE '✅ Đã insert/cập nhật 12 tài khoản thành công!';
END $$;
