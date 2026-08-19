-- ============================================================
-- V14: Add Cancellation Request Fields to Shop Orders
-- ============================================================

ALTER TABLE shop_orders 
ADD COLUMN cancellation_requested BOOLEAN NOT NULL DEFAULT false,
ADD COLUMN cancellation_reason VARCHAR(255);
