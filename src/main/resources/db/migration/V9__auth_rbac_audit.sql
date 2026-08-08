
-- ============================================================
-- V9: AUTH SECURITY, OTP, RBAC AND AUDIT LOG
-- Depends on: users, roles from V1-V8
-- ============================================================

-- 1 Account security
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS login_fail_count INTEGER
        NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS locked_until TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS phone VARCHAR(20),
    ADD COLUMN IF NOT EXISTS mfa_enabled BOOLEAN
        NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS mfa_secret VARCHAR(255);

ALTER TABLE users
    ADD CONSTRAINT ck_users_login_fail_count
        CHECK (login_fail_count >= 0);

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_phone
    ON users(phone)
    WHERE phone IS NOT NULL;

CREATE INDEX IF NOT EXISTS ix_users_locked_until
    ON users(locked_until)
    WHERE locked_until IS NOT NULL;


-- 2 OTP requests
-- Chỉ lưu OTP hash, không lưu OTP rõ.
CREATE TABLE otp_requests (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL,
    otp_hash        VARCHAR(255) NOT NULL,
    type            VARCHAR(20) NOT NULL,
    expires_at      TIMESTAMPTZ NOT NULL,
    used            BOOLEAN NOT NULL DEFAULT FALSE,
    attempt_count   INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL
                    DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_otp_requests_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT ck_otp_requests_type
        CHECK (type IN ('LOGIN', 'MFA', 'RESET_PW')),

    CONSTRAINT ck_otp_requests_attempt_count
        CHECK (attempt_count >= 0)
);

CREATE INDEX ix_otp_requests_user_type
    ON otp_requests(user_id, type);

CREATE INDEX ix_otp_requests_expires_at
    ON otp_requests(expires_at);

CREATE INDEX ix_otp_requests_active
    ON otp_requests(user_id, type, expires_at)
    WHERE used = FALSE;


-- 3 Permissions
CREATE TABLE permissions (
    id              UUID PRIMARY KEY,
    resource        VARCHAR(100) NOT NULL,
    action          VARCHAR(20) NOT NULL,
    description     VARCHAR(255),

    CONSTRAINT uk_permissions_resource_action
        UNIQUE (resource, action),

    CONSTRAINT ck_permissions_action
        CHECK (
            action IN (
                'CREATE',
                'READ',
                'UPDATE',
                'DELETE',
                'APPROVE',
                'EXPORT'
            )
        )
);

CREATE INDEX ix_permissions_resource
    ON permissions(resource);


-- 4 Role permissions
CREATE TABLE role_permissions (
    role_id         UUID NOT NULL,
    permission_id   UUID NOT NULL,

    CONSTRAINT pk_role_permissions
        PRIMARY KEY (role_id, permission_id),

    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_role_permissions_permission
        FOREIGN KEY (permission_id)
        REFERENCES permissions(id)
        ON DELETE CASCADE
);

CREATE INDEX ix_role_permissions_permission
    ON role_permissions(permission_id);


-- 5 Audit logs
CREATE TABLE audit_logs (
    id              UUID PRIMARY KEY,
    table_name      VARCHAR(100) NOT NULL,
    record_id       UUID NOT NULL,
    action          VARCHAR(20) NOT NULL,
    old_data        JSONB,
    new_data        JSONB,
    created_by      UUID,
    created_at      TIMESTAMPTZ NOT NULL
                    DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_logs_created_by
        FOREIGN KEY (created_by)
        REFERENCES users(id)
        ON DELETE SET NULL,

    CONSTRAINT ck_audit_logs_action
        CHECK (action IN ('INSERT', 'UPDATE', 'DELETE'))
);

CREATE INDEX ix_audit_logs_table_record
    ON audit_logs(table_name, record_id);

CREATE INDEX ix_audit_logs_created_at
    ON audit_logs(created_at DESC);

CREATE INDEX ix_audit_logs_created_by
    ON audit_logs(created_by)
    WHERE created_by IS NOT NULL;

CREATE INDEX ix_audit_logs_old_data
    ON audit_logs USING GIN(old_data);

CREATE INDEX ix_audit_logs_new_data
    ON audit_logs USING GIN(new_data);


-- 6 Documentation
COMMENT ON COLUMN users.mfa_secret IS
    'Encrypted MFA secret; never store plaintext';

COMMENT ON COLUMN otp_requests.otp_hash IS
    'One-way hash of OTP; never store plaintext OTP';

COMMENT ON TABLE audit_logs IS
    'Immutable application audit trail; excludes credentials and tokens';
