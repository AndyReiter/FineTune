-- Add completed_date column to work_orders table
-- This field tracks when a work order was completed (all items picked up)
-- Nullable because existing records won't have a completion date

ALTER TABLE work_orders 
ADD COLUMN completed_date TIMESTAMP NULL;
