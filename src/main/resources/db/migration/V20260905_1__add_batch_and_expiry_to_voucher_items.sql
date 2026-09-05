ALTER TABLE stock_voucher_items
ADD COLUMN batch_code VARCHAR(100),
ADD COLUMN expiry_date DATE;
