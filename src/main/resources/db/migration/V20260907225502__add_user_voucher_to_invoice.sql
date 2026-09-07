DO $$ 
BEGIN 
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='invoices' AND column_name='user_voucher_id') THEN 
    ALTER TABLE invoices ADD COLUMN user_voucher_id UUID;
    ALTER TABLE invoices ADD CONSTRAINT fk_invoice_user_voucher FOREIGN KEY (user_voucher_id) REFERENCES user_vouchers(id);
  END IF; 
END $$;
