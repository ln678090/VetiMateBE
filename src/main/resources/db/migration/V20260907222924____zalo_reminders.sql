CREATE TABLE zalo_reminders (
    id UUID PRIMARY KEY,

    customer_id UUID NOT NULL
        REFERENCES clinic_customers(id),

    pet_id UUID NOT NULL
        REFERENCES clinic_pets(id),

    phone VARCHAR(20) NOT NULL,
    pet_name VARCHAR(100) NOT NULL,

    reminder_type VARCHAR(30) NOT NULL
        CHECK (reminder_type IN ('VACCINATION', 'FOLLOW_UP')),

    due_date DATE NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (
            status IN (
                'PENDING',
                'PROCESSING',
                'SENT',
                'FAILED'
            )
        ),

    idempotency_key VARCHAR(200) NOT NULL UNIQUE,

    attempt_count INTEGER NOT NULL DEFAULT 0,

    next_attempt_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    locked_until TIMESTAMPTZ,
    sent_at TIMESTAMPTZ,
    last_error TEXT,

    created_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_zalo_reminders_dispatch
    ON zalo_reminders (
        status,
        due_date,
        next_attempt_at
    );

CREATE TABLE zalo_daily_quotas (
    quota_date DATE PRIMARY KEY,

    attempt_count INTEGER NOT NULL DEFAULT 0
        CHECK (attempt_count >= 0 AND attempt_count <= 100),

    last_attempt_at TIMESTAMPTZ
);
