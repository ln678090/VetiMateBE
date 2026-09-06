-- V20260903_5__invoice_review_add_product.sql

ALTER TABLE invoice_reviews
ADD COLUMN product_id UUID;

-- We don't make it NOT NULL immediately if there's data, but since we are in dev we assume it's mostly empty or we can just leave it nullable in DB and handle in application layer, 
-- or we can delete all existing records and set it to NOT NULL.
DELETE FROM invoice_reviews;

ALTER TABLE invoice_reviews
ALTER COLUMN product_id SET NOT NULL;

ALTER TABLE invoice_reviews
ADD CONSTRAINT fk_invoice_reviews_product 
FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE;
