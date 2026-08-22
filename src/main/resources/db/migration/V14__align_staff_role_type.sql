
ALTER TABLE staff
    DROP CONSTRAINT IF EXISTS ck_staff_role_type;

UPDATE staff
SET role_type = 'SHOP_STAFF'
WHERE role_type = 'SHOP';

ALTER TABLE staff
    ADD CONSTRAINT ck_staff_role_type
    CHECK (
        role_type IN (
            'DOCTOR',
            'RECEPTIONIST',
            'MANAGER',
            'ACCOUNTANT',
            'WAREHOUSE',
            'SHOP_STAFF'
        )
    );
