UPDATE products
SET 
  review_count = COALESCE((SELECT COUNT(*) FROM invoice_reviews WHERE invoice_reviews.product_id = products.id), 0),
  rating = COALESCE((SELECT AVG(rating) FROM invoice_reviews WHERE invoice_reviews.product_id = products.id), 0.0);
