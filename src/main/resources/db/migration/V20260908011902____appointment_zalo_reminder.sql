
ALTER TABLE zalo_reminders
    DROP CONSTRAINT IF EXISTS zalo_reminders_reminder_type_check;

ALTER TABLE zalo_reminders
    ADD CONSTRAINT zalo_reminders_reminder_type_check
    CHECK (
        reminder_type IN (
            'VACCINATION',
            'FOLLOW_UP',
            'APPOINTMENT'
        )
    );

ALTER TABLE zalo_reminders
    ADD COLUMN appointment_id UUID;

ALTER TABLE zalo_reminders
    ADD CONSTRAINT fk_zalo_reminder_appointment
    FOREIGN KEY (appointment_id)
    REFERENCES clinic_appointments(id)
    ON DELETE CASCADE;

CREATE UNIQUE INDEX uq_zalo_reminder_appointment
    ON zalo_reminders(appointment_id)
    WHERE appointment_id IS NOT NULL;
