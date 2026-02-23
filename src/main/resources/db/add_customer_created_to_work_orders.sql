-- Add customer_created column to work_orders table
-- This field tracks whether the work order was created by a customer (TRUE) or staff member (FALSE)
-- Default value is TRUE for new customer-initiated work orders

ALTER TABLE work_orders 
ADD COLUMN customer_created BOOLEAN DEFAULT TRUE NOT NULL;

-- Update existing records to default to TRUE (assume existing orders were customer-created)
UPDATE work_orders 
SET customer_created = TRUE 
WHERE customer_created IS NULL;
