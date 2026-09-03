DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'invoices') THEN
        CREATE TABLE invoices (
            discount_amount numeric(15,2) not null, 
            subtotal numeric(15,2) not null, 
            total_amount numeric(15,2) not null, 
            created_at timestamp(6) with time zone not null, 
            paid_at timestamp(6) with time zone, 
            updated_at timestamp(6) with time zone not null, 
            created_by uuid, 
            customer_id uuid not null, 
            id uuid not null, 
            parent_invoice_id uuid, 
            pet_id uuid, 
            status varchar(20) not null, 
            type varchar(20) not null, 
            payment_method varchar(30), 
            invoice_code varchar(50) not null unique, 
            note varchar(500), 
            primary key (id)
        );
        ALTER TABLE invoices ADD CONSTRAINT FKqngwowel20k8h8kq6uvqlyxe1 FOREIGN KEY (customer_id) REFERENCES clinic_customers;
        ALTER TABLE invoices ADD CONSTRAINT FKr8pvctqnkveaoukj31899xi08 FOREIGN KEY (pet_id) REFERENCES clinic_pets;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'invoice_items') THEN
        CREATE TABLE invoice_items (
            quantity numeric(10,2) not null, 
            total numeric(15,2) not null, 
            unit_price numeric(12,2) not null, 
            created_at timestamp(6) with time zone not null, 
            id uuid not null, 
            invoice_id uuid not null, 
            medicine_id uuid, 
            product_id uuid, 
            service_id uuid, 
            name_snapshot varchar(255) not null, 
            primary key (id)
        );
        ALTER TABLE invoice_items ADD CONSTRAINT FK46ae0lhu1oqs7cv91fn6y9n7w FOREIGN KEY (invoice_id) REFERENCES invoices;
        ALTER TABLE invoice_items ADD CONSTRAINT FKs3tu9gmkgshq8oeq5n0rinxeu FOREIGN KEY (product_id) REFERENCES products;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'user_favorite_products') THEN
        CREATE TABLE user_favorite_products (
            created_at timestamp(6) with time zone, 
            product_id uuid not null, 
            user_id uuid not null, 
            primary key (product_id, user_id)
        );
        ALTER TABLE user_favorite_products ADD CONSTRAINT FK85wif3w88mq3p4ad5h972g9n9 FOREIGN KEY (product_id) REFERENCES products;
        ALTER TABLE user_favorite_products ADD CONSTRAINT FK15xaakjffx56a52b4dulxa0tu FOREIGN KEY (user_id) REFERENCES users;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'user_viewed_products') THEN
        CREATE TABLE user_viewed_products (
            viewed_at timestamp(6) with time zone, 
            product_id uuid not null, 
            user_id uuid not null, 
            primary key (product_id, user_id)
        );
        ALTER TABLE user_viewed_products ADD CONSTRAINT FK5miak2ejex21dk7l9fnd8a1hb FOREIGN KEY (product_id) REFERENCES products;
        ALTER TABLE user_viewed_products ADD CONSTRAINT FK3madhr27u9cmqyh4v0v5w5nk1 FOREIGN KEY (user_id) REFERENCES users;
    END IF;
END $$;

ALTER TABLE invoices DROP CONSTRAINT IF EXISTS ck_invoices_status;
ALTER TABLE invoices ADD CONSTRAINT ck_invoices_status CHECK (status IN ('DRAFT', 'PENDING', 'CONFIRMED', 'SHIPPING', 'DELIVERED', 'PAID', 'CANCELLED'));

ALTER TABLE clinic_appointments ADD COLUMN IF NOT EXISTS is_called_to_confirm BOOLEAN NOT NULL DEFAULT FALSE;
