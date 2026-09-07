-- ============================================================
-- V20260907_1: Add Warehouse Locations to Stock Batches & Vouchers
-- Supports separating "Kho bảo quản" (STORAGE) & "Kho bác sĩ" (DOCTOR)
-- ============================================================

-- 1. Add warehouse location column to stock_batches
ALTER TABLE stock_batches
    ADD COLUMN IF NOT EXISTS warehouse VARCHAR(30) NOT NULL DEFAULT 'STORAGE';

-- 2. Update unique constraints to include warehouse so a batch code can exist in both STORAGE and DOCTOR
DROP INDEX IF EXISTS ux_stock_batches_medicine_code;
DROP INDEX IF EXISTS ux_stock_batches_product_code;

CREATE UNIQUE INDEX ux_stock_batches_medicine_code
    ON stock_batches(medicine_id, batch_code, warehouse)
    WHERE medicine_id IS NOT NULL
      AND batch_code IS NOT NULL;

CREATE UNIQUE INDEX ux_stock_batches_product_code
    ON stock_batches(product_id, batch_code, warehouse)
    WHERE product_id IS NOT NULL
      AND batch_code IS NOT NULL;

CREATE INDEX IF NOT EXISTS ix_stock_batches_warehouse
    ON stock_batches(warehouse);

CREATE INDEX IF NOT EXISTS ix_stock_batches_warehouse_expiry
    ON stock_batches(warehouse, expiry_date)
    WHERE remaining_qty > 0;

-- 3. Add source and destination warehouse columns to stock_vouchers
ALTER TABLE stock_vouchers
    ADD COLUMN IF NOT EXISTS source_warehouse VARCHAR(30) DEFAULT 'STORAGE',
    ADD COLUMN IF NOT EXISTS destination_warehouse VARCHAR(30);

COMMENT ON COLUMN stock_batches.warehouse IS 'Warehouse location: STORAGE (Kho bảo quản) or DOCTOR (Kho bác sĩ)';
COMMENT ON COLUMN stock_vouchers.source_warehouse IS 'Source warehouse location for export/transfer';
COMMENT ON COLUMN stock_vouchers.destination_warehouse IS 'Target warehouse location for import/transfer/export-to-doctor';
