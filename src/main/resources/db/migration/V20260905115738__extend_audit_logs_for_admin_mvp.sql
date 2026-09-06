-- Extend V9 audit_logs for the minimal Admin Audit Log feature.
-- Never store passwords, tokens, OTP values, MFA secrets or cookies.

ALTER TABLE audit_logs
    DROP CONSTRAINT IF EXISTS ck_audit_logs_action;

ALTER TABLE audit_logs
    ALTER COLUMN record_id DROP NOT NULL;

ALTER TABLE audit_logs
    ADD COLUMN IF NOT EXISTS module VARCHAR(50),
    ADD COLUMN IF NOT EXISTS actor_identifier VARCHAR(255),
    ADD COLUMN IF NOT EXISTS description VARCHAR(500),
    ADD COLUMN IF NOT EXISTS ip_address VARCHAR(45),
    ADD COLUMN IF NOT EXISTS user_agent VARCHAR(500);

UPDATE audit_logs
SET module = COALESCE(NULLIF(table_name, ''), 'SYSTEM')
WHERE module IS NULL;

ALTER TABLE audit_logs
    ALTER COLUMN module SET NOT NULL;

ALTER TABLE audit_logs
    ADD CONSTRAINT ck_audit_logs_action
        CHECK (
            action IN (
                'INSERT',
                'UPDATE',
                'DELETE',
                'LOGIN',
                'ROLE_CHANGE'
            )
        );

CREATE INDEX IF NOT EXISTS ix_audit_logs_module_created_at
    ON audit_logs(module, created_at DESC);

CREATE INDEX IF NOT EXISTS ix_audit_logs_action_created_at
    ON audit_logs(action, created_at DESC);

CREATE INDEX IF NOT EXISTS ix_audit_logs_actor_identifier
    ON audit_logs(actor_identifier)
    WHERE actor_identifier IS NOT NULL;

COMMENT ON COLUMN audit_logs.module IS
    'Business module: AUTH, RBAC, CLINIC, SHOP, INVENTORY, BILLING';

COMMENT ON COLUMN audit_logs.actor_identifier IS
    'Snapshot of actor email or username for traceability';

COMMENT ON COLUMN audit_logs.description IS
    'Human-readable summary excluding credentials and secrets';

COMMENT ON COLUMN audit_logs.ip_address IS
    'Request source IP when available';

COMMENT ON COLUMN audit_logs.user_agent IS
    'Request user-agent truncated to 500 characters';

COMMENT ON TABLE audit_logs IS
    'Immutable application audit trail; application users cannot update or delete records';
