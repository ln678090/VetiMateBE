-- V20260903_3__invoice_review.sql

ALTER TABLE invoices ADD COLUMN is_reviewed BOOLEAN DEFAULT FALSE;
