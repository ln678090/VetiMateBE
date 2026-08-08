-- V7: Thêm cột phone vào users để liên lạc với khách hàng.
-- NULLABLE: user cũ đăng ký trước đó không có phone -> tránh vỡ dữ liệu (backfill sau).
-- Ràng buộc "bắt buộc" nằm ở tầng application (RegisterRequest validation), không ở DB.
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(20);

-- (Tùy chọn) index nếu sau này tra cứu/đăng nhập bằng phone:
 CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone);