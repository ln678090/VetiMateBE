UPDATE products p
SET stock_quantity = COALESCE((
    SELECT SUM(sb.remaining_qty)
    FROM stock_batches sb
    WHERE sb.product_id = p.id
), 0);
