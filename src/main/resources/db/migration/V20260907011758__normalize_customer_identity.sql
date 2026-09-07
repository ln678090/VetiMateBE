ALTER TABLE public.users
    ADD COLUMN IF NOT EXISTS address VARCHAR(255);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM public.clinic_customers
        WHERE user_id IS NULL
    ) THEN
        RAISE EXCEPTION
            'Cannot normalize clinic_customers: rows with NULL user_id still exist';
    END IF;
END
$$;

UPDATE public.users AS account
SET address = customer.address
FROM public.clinic_customers AS customer
WHERE customer.user_id = account.id
  AND account.address IS NULL
  AND customer.address IS NOT NULL;

ALTER TABLE public.clinic_customers
    ALTER COLUMN user_id SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS
    uk_clinic_customers_user_id
ON public.clinic_customers(user_id);

ALTER TABLE public.clinic_customers
    DROP COLUMN IF EXISTS full_name,
    DROP COLUMN IF EXISTS phone,
    DROP COLUMN IF EXISTS email,
    DROP COLUMN IF EXISTS address;
