-- Add notes TEXT column to work_orders table
-- This field stores intake review notes and general work order comments

ALTER TABLE work_orders 
ADD COLUMN notes TEXT;

-- Add comment for documentation
-- COMMENT ON COLUMN work_orders.notes IS 'General notes and comments for the work order, primarily set during intake review';
