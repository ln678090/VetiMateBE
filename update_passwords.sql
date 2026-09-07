-- =============================================================
-- Cập nhật password cho tất cả tài khoản seed → "123456"
-- =============================================================
UPDATE users
SET password = '$argon2id$v=19$m=16384,t=2,p=1$1mf/Inv49YEOFO/zPdDbAw$a8O3kDisoTld8Gs3aGM6bnAl2GFkEATzdnjiB43+TNg'
WHERE email IN (
    'user12@gmail.com',
    'admin@gmail.com',
    'receptionist@gmail.com',
    'customer@gmail.com',
    'manager@gmail.com',
    'doctor1@gmail.com',
    'accountant@gmail.com',
    'warehouse@gmail.com',
    'shopstaff@gmail.com',
    'systempartner@gmail.com',
    'hihi1@gmail.com',
    'staff01@vetimate.vn'
);
