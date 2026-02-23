-- Create staff_settings table
-- This table stores global settings for staff and customer interactions
-- Currently supports configuring daily limits for customer-created work orders

CREATE TABLE staff_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    max_customer_work_orders_per_day INT DEFAULT 25 NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Insert default settings row
-- This ensures at least one settings record exists for the application
INSERT INTO staff_settings (max_customer_work_orders_per_day) 
VALUES (25);
