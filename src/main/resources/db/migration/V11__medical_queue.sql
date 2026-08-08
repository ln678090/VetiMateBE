
-- ============================================================
-- V11: MEDICAL RECORDS, PRESCRIPTIONS AND SMART QUEUE
-- Depends on:
-- clinic_appointments, clinic_pets, clinic_services,
-- staff, medicines
-- ============================================================

-- 1. MEDICAL RECORDS
CREATE TABLE medical_records (
    id                  UUID PRIMARY KEY,
    appointment_id      UUID NOT NULL,
    pet_id              UUID NOT NULL,
    doctor_id           UUID NOT NULL,
    symptoms            TEXT,
    diagnosis           TEXT,
    treatment_plan      TEXT,
    weight_kg           NUMERIC(6,2),
    doctor_note         TEXT,
    status              VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_medical_records_appointment
        FOREIGN KEY (appointment_id)
        REFERENCES clinic_appointments(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_medical_records_pet
        FOREIGN KEY (pet_id)
        REFERENCES clinic_pets(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_medical_records_doctor
        FOREIGN KEY (doctor_id)
        REFERENCES staff(id)
        ON DELETE RESTRICT,

    CONSTRAINT uk_medical_records_appointment
        UNIQUE (appointment_id),

    CONSTRAINT ck_medical_records_weight
        CHECK (weight_kg IS NULL OR weight_kg > 0),

    CONSTRAINT ck_medical_records_status
        CHECK (status IN ('IN_PROGRESS', 'COMPLETED'))
);

CREATE INDEX ix_medical_records_pet_created
    ON medical_records(pet_id, created_at DESC);

CREATE INDEX ix_medical_records_doctor_created
    ON medical_records(doctor_id, created_at DESC);

CREATE INDEX ix_medical_records_status
    ON medical_records(status);


-- 2. SERVICE INDICATIONS
CREATE TABLE service_indications (
    id                  UUID PRIMARY KEY,
    medical_record_id   UUID NOT NULL,
    service_id          UUID NOT NULL,
    status              VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    result_note         TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_service_indications_record
        FOREIGN KEY (medical_record_id)
        REFERENCES medical_records(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_service_indications_service
        FOREIGN KEY (service_id)
        REFERENCES clinic_services(id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_service_indications_status
        CHECK (status IN ('PENDING', 'DONE', 'CANCELLED'))
);

CREATE INDEX ix_service_indications_record
    ON service_indications(medical_record_id);

CREATE INDEX ix_service_indications_service_status
    ON service_indications(service_id, status);


-- 3. PRESCRIPTIONS
CREATE TABLE prescriptions (
    id                  UUID PRIMARY KEY,
    medical_record_id   UUID NOT NULL,
    medicine_id         UUID NOT NULL,
    quantity            NUMERIC(10,2) NOT NULL,
    dosage              VARCHAR(200) NOT NULL,
    duration_days       INTEGER,
    note                VARCHAR(500),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_prescriptions_record
        FOREIGN KEY (medical_record_id)
        REFERENCES medical_records(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_prescriptions_medicine
        FOREIGN KEY (medicine_id)
        REFERENCES medicines(id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_prescriptions_quantity
        CHECK (quantity > 0),

    CONSTRAINT ck_prescriptions_duration
        CHECK (duration_days IS NULL OR duration_days > 0)
);

CREATE INDEX ix_prescriptions_record
    ON prescriptions(medical_record_id);

CREATE INDEX ix_prescriptions_medicine
    ON prescriptions(medicine_id);


-- 4. PET ALLERGIES
CREATE TABLE pet_allergies (
    id                  UUID PRIMARY KEY,
    pet_id              UUID NOT NULL,
    medicine_id         UUID,
    allergen            VARCHAR(200),
    severity            VARCHAR(20) NOT NULL DEFAULT 'HIGH',
    note                VARCHAR(500),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_pet_allergies_pet
        FOREIGN KEY (pet_id)
        REFERENCES clinic_pets(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_pet_allergies_medicine
        FOREIGN KEY (medicine_id)
        REFERENCES medicines(id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_pet_allergies_source
        CHECK (
            medicine_id IS NOT NULL
            OR NULLIF(BTRIM(allergen), '') IS NOT NULL
        ),

    CONSTRAINT ck_pet_allergies_severity
        CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'))
);

CREATE INDEX ix_pet_allergies_pet
    ON pet_allergies(pet_id);

CREATE INDEX ix_pet_allergies_medicine
    ON pet_allergies(medicine_id)
    WHERE medicine_id IS NOT NULL;


-- 5. DRUG INTERACTIONS
-- Lưu cặp theo thứ tự medicine_a_id < medicine_b_id.
CREATE TABLE drug_interactions (
    id                  UUID PRIMARY KEY,
    medicine_a_id       UUID NOT NULL,
    medicine_b_id       UUID NOT NULL,
    severity            VARCHAR(20) NOT NULL DEFAULT 'HIGH',
    description         TEXT NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_drug_interactions_a
        FOREIGN KEY (medicine_a_id)
        REFERENCES medicines(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_drug_interactions_b
        FOREIGN KEY (medicine_b_id)
        REFERENCES medicines(id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_drug_interactions_order
        CHECK (medicine_a_id < medicine_b_id),

    CONSTRAINT uk_drug_interactions_pair
        UNIQUE (medicine_a_id, medicine_b_id),

    CONSTRAINT ck_drug_interactions_severity
        CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'))
);

CREATE INDEX ix_drug_interactions_medicine_b
    ON drug_interactions(medicine_b_id);


-- 6. SMART QUEUE
CREATE TABLE queue_tickets (
    id                  UUID PRIMARY KEY,
    appointment_id      UUID,
    queue_date          DATE NOT NULL DEFAULT CURRENT_DATE,
    queue_type          VARCHAR(20) NOT NULL,
    ticket_number       INTEGER NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    called_at           TIMESTAMPTZ,
    completed_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_queue_tickets_appointment
        FOREIGN KEY (appointment_id)
        REFERENCES clinic_appointments(id)
        ON DELETE RESTRICT,

    CONSTRAINT uk_queue_tickets_appointment
        UNIQUE (appointment_id),

    CONSTRAINT uk_queue_tickets_daily_number
        UNIQUE (queue_date, queue_type, ticket_number),

    CONSTRAINT ck_queue_tickets_type
        CHECK (queue_type IN ('CLINIC', 'SPA')),

    CONSTRAINT ck_queue_tickets_number
        CHECK (ticket_number > 0),

    CONSTRAINT ck_queue_tickets_status
        CHECK (
            status IN (
                'WAITING',
                'CALLED',
                'DONE',
                'CANCELLED'
            )
        ),

    CONSTRAINT ck_queue_tickets_called_at
        CHECK (
            status NOT IN ('CALLED', 'DONE')
            OR called_at IS NOT NULL
        ),

    CONSTRAINT ck_queue_tickets_completed_at
        CHECK (
            status <> 'DONE'
            OR completed_at IS NOT NULL
        )
);

CREATE INDEX ix_queue_tickets_daily_status
    ON queue_tickets(queue_date, queue_type, status, ticket_number);

CREATE INDEX ix_queue_tickets_waiting
    ON queue_tickets(queue_date, queue_type, ticket_number)
    WHERE status = 'WAITING';


-- 7. DOCUMENTATION
COMMENT ON TABLE medical_records IS
    'Clinical record created from one appointment';

COMMENT ON TABLE drug_interactions IS
    'Canonical medicine interaction pairs: medicine_a_id < medicine_b_id';

COMMENT ON TABLE queue_tickets IS
    'Daily electronic queue for clinic and spa appointments';
