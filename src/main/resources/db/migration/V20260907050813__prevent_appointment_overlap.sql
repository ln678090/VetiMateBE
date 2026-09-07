CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE clinic_appointments
    ADD CONSTRAINT ex_appointments_service_time_overlap
    EXCLUDE USING gist (
        service_id WITH =,
        tstzrange(start_at, end_at, '[)') WITH &&
    )
    WHERE (status <> 'CANCELLED');
