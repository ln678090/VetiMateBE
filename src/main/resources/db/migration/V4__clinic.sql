-- ============================================================
-- 4__clinic.sql  |  Module PHÒNG KHÁM (Customer; Pet; Service; Appointment)
-- Spring Boot 3,50,14 | UUIDv7 sinh từ Java (@UuidV7) -> KHÔNG dùng gen_random_uuid()
-- ============================================================

-- 1) KHÁCH HÀNG (chủ pet). userId nullable: link User nếu khách có tài khoản
CREATE TABLE clinic_customers (
    id           UUID         PRIMARY KEY,
    user_id      UUID         NULL REFERENCES users(id) ON DELETE SET NULL,
    full_name    VARCHAR(150) NOT NULL,
    phone        VARCHAR(20)  NOT NULL,
    email        VARCHAR(150) NULL,
    address      VARCHAR(255) NULL,
    note         VARCHAR(500) NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
-- 1 tài khoản chỉ nên gắn 1 hồ sơ khách (nếu có user_id thì unique)
CREATE UNIQUE INDEX ux_clinic_customers_user_id ON clinic_customers(user_id) WHERE user_id IS NOT NULL;
CREATE INDEX ix_clinic_customers_phone ON clinic_customers(phone);

-- 2) PET (thú cưng) thuộc về 1 khách
CREATE TABLE clinic_pets (
    id           UUID         PRIMARY KEY,
    customer_id  UUID         NOT NULL REFERENCES clinic_customers(id) ON DELETE CASCADE,
    name         VARCHAR(100) NOT NULL,
    species      VARCHAR(30)  NOT NULL,           -- DOG / CAT / OTHER
    breed        VARCHAR(100) NULL,
    gender       VARCHAR(10)  NULL,               -- MALE / FEMALE / UNKNOWN
    birth_date   DATE         NULL,
    weight_kg    NUMERIC(6,2) NULL,
    note         VARCHAR(500) NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX ix_clinic_pets_customer_id ON clinic_pets(customer_id);

-- 3) DỊCH VỤ khám/spa (bảng giá + thời lượng chuẩn)  -- STT 7
CREATE TABLE clinic_services (
    id            UUID         PRIMARY KEY,
    name          VARCHAR(150) NOT NULL,
    description   VARCHAR(500) NULL,
    price         NUMERIC(12,2) NOT NULL DEFAULT 0,
    duration_min  INTEGER      NOT NULL DEFAULT 30 CHECK (duration_min > 0),
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX ix_clinic_services_active ON clinic_services(is_active);

-- 4) LỊCH KHÁM (đặt lịch)  -- STT 22 (vòng 1: booking)
CREATE TABLE clinic_appointments (
    id             UUID          PRIMARY KEY,
    customer_id    UUID          NOT NULL REFERENCES clinic_customers(id) ON DELETE CASCADE,
    pet_id         UUID          NOT NULL REFERENCES clinic_pets(id)      ON DELETE CASCADE,
    service_id     UUID          NOT NULL REFERENCES clinic_services(id)  ON DELETE RESTRICT,
    start_at       TIMESTAMPTZ   NOT NULL,
    end_at         TIMESTAMPTZ   NOT NULL,
    duration_min   INTEGER       NOT NULL,             -- snapshot thời lượng lúc đặt
    price_snapshot NUMERIC(12,2) NOT NULL,             -- snapshot giá lúc đặt
    status         VARCHAR(20)   NOT NULL DEFAULT 'SCHEDULED',
    note           VARCHAR(500)  NULL,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_appt_time CHECK (end_at > start_at)
);
CREATE INDEX ix_appt_start_at   ON clinic_appointments(start_at);
CREATE INDEX ix_appt_pet_id     ON clinic_appointments(pet_id);
CREATE INDEX ix_appt_service_id ON clinic_appointments(service_id);
CREATE INDEX ix_appt_status     ON clinic_appointments(status);
