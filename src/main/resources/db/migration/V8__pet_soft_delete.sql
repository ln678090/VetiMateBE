-- V8__pet_soft_delete.sql
-- Add soft delete column to clinic_pets

ALTER TABLE clinic_pets 
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ DEFAULT NULL;

-- Index for filtering non-deleted pets
CREATE INDEX IF NOT EXISTS idx_clinic_pets_deleted_at 
ON clinic_pets(deleted_at) WHERE deleted_at IS NULL;

COMMENT ON COLUMN clinic_pets.deleted_at IS 'Soft delete timestamp. NULL = active, NOT NULL = deleted';

