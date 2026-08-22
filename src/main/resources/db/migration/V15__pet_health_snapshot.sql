ALTER TABLE medical_records
    ADD COLUMN health_status VARCHAR(30) NOT NULL DEFAULT 'MONITORING';

ALTER TABLE medical_records
    ADD CONSTRAINT chk_medical_records_health_status
        CHECK (
            health_status IN (
                'HEALTHY',
                'MONITORING',
                'TREATMENT',
                'CRITICAL',
                'RECOVERING'
            )
        );

ALTER TABLE clinic_pets
    ADD COLUMN current_health_status VARCHAR(30),
    ADD COLUMN current_health_note TEXT,
    ADD COLUMN last_examined_at TIMESTAMPTZ;

ALTER TABLE clinic_pets
    ADD CONSTRAINT chk_clinic_pets_health_status
        CHECK (
            current_health_status IS NULL
            OR current_health_status IN (
                'HEALTHY',
                'MONITORING',
                'TREATMENT',
                'CRITICAL',
                'RECOVERING'
            )
        );

CREATE INDEX idx_medical_records_pet_updated
    ON medical_records (pet_id, updated_at DESC);
