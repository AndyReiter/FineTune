-- Add logoUrl column to Shop table
ALTER TABLE shop ADD COLUMN IF NOT EXISTS logo_url VARCHAR(500);
