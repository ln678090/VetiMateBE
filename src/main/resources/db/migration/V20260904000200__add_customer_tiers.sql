-- 1. Add total_spending and tier to user_loyalty_points
ALTER TABLE user_loyalty_points
ADD COLUMN total_spending DECIMAL(15,2) DEFAULT 0.0,
ADD COLUMN tier VARCHAR(20) DEFAULT 'MEMBER';

-- 2. Calculate historical total_spending from invoices
-- We sum total_amount from invoices where status = 'COMPLETED' or 'PAID'.
-- Actually the status for paid invoices is 'PAID' according to Invoice.java (status: 'DRAFT', 'PAID', 'CANCELLED').
UPDATE user_loyalty_points u
SET total_spending = COALESCE((
    SELECT SUM(i.total_amount)
    FROM invoices i
    JOIN clinic_customers c ON c.id = i.customer_id
    WHERE c.user_id = u.user_id AND i.status = 'PAID'
), 0.0);

-- 3. Update tier based on the total_spending
UPDATE user_loyalty_points
SET tier = 
    CASE 
        WHEN total_spending >= 15000000 THEN 'DIAMOND'
        WHEN total_spending >= 10000000 THEN 'GOLD'
        WHEN total_spending >= 7000000 THEN 'SILVER'
        WHEN total_spending >= 3000000 THEN 'BRONZE'
        ELSE 'MEMBER'
    END;

-- 4. Add required_tier to vouchers
ALTER TABLE vouchers
ADD COLUMN required_tier VARCHAR(20);
