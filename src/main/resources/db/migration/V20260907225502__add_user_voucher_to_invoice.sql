ALTER TABLE invoices ADD COLUMN user_voucher_id UUID;
ALTER TABLE invoices ADD CONSTRAINT fk_invoice_user_voucher FOREIGN KEY (user_voucher_id) REFERENCES user_vouchers(id);
