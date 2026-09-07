ALTER TABLE stock_voucher_items ADD COLUMN supplier_id UUID;
ALTER TABLE stock_voucher_items ADD CONSTRAINT fk_stock_voucher_items_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL;
