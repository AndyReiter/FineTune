-- MySQL Migration Script for FineTune Application
-- This script creates all tables and relationships based on Hibernate entity definitions

CREATE TABLE customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    firstName VARCHAR(255),
    lastName VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(255),
    heightInches INT,
    weight INT,
    skiAbilityLevel VARCHAR(32),
    UNIQUE KEY unique_email_phone (email, phone)
);

CREATE TABLE boots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    brand VARCHAR(255) NOT NULL,
    model VARCHAR(255) NOT NULL,
    bsl INT NOT NULL,
    heightInches INT,
    weight INT,
    age INT,
    abilityLevel VARCHAR(32),
    active BOOLEAN DEFAULT TRUE,
    customer_id BIGINT NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE work_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    status VARCHAR(255),
    createdAt DATETIME,
    promised_by DATE,
    completed_date DATETIME,
    customer_created BOOLEAN DEFAULT FALSE,
    notes TEXT,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE equipment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(32) NOT NULL,
    brand VARCHAR(255),
    model VARCHAR(255),
    length INT,
    serviceType VARCHAR(255),
    condition VARCHAR(32),
    bindingBrand VARCHAR(255),
    bindingModel VARCHAR(255),
    heightInches INT,
    weight INT,
    age INT,
    abilityLevel VARCHAR(32),
    boot_id BIGINT,
    status VARCHAR(32) DEFAULT 'PENDING',
    last_serviced_date DATE,
    last_service_type VARCHAR(255),
    work_order_id BIGINT,
    customer_id BIGINT NOT NULL,
    FOREIGN KEY (boot_id) REFERENCES boots(id),
    FOREIGN KEY (work_order_id) REFERENCES work_orders(id),
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE work_order_notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_order_id BIGINT NOT NULL,
    note_text TEXT NOT NULL,
    created_by VARCHAR(100),
    created_at DATETIME NOT NULL,
    FOREIGN KEY (work_order_id) REFERENCES work_orders(id)
);

CREATE TABLE staff (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(32) DEFAULT 'ROLE_STAFF',
    created_at DATETIME,
    updated_at DATETIME
);

CREATE TABLE staff_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    max_customer_work_orders_per_day INT NOT NULL DEFAULT 25,
    updated_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL
);

CREATE TABLE locations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    address VARCHAR(255),
    city VARCHAR(255),
    state VARCHAR(255),
    zipCode VARCHAR(32)
);

CREATE TABLE shops (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    status VARCHAR(32),
    logoUrl VARCHAR(255),
    location_id BIGINT,
    FOREIGN KEY (location_id) REFERENCES locations(id)
);

CREATE TABLE ski_brands (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE ski_models (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    brand_id BIGINT NOT NULL,
    FOREIGN KEY (brand_id) REFERENCES ski_brands(id)
);

CREATE TABLE agreement_templates (
    id VARCHAR(36) PRIMARY KEY,
    shop_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    agreement_text TEXT NOT NULL,
    logo_url VARCHAR(255),
    jurisdiction_state VARCHAR(64),
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (shop_id) REFERENCES shops(id)
);

CREATE TABLE signed_agreements (
    id VARCHAR(36) PRIMARY KEY,
    work_order_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    agreement_template_id VARCHAR(36) NOT NULL,
    pdf_storage_key VARCHAR(255) NOT NULL,
    signature_name VARCHAR(255) NOT NULL,
    signature_ip VARCHAR(64),
    signature_user_agent VARCHAR(255),
    signed_at DATETIME NOT NULL,
    document_hash VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (work_order_id) REFERENCES work_orders(id),
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (agreement_template_id) REFERENCES agreement_templates(id)
);
