CREATE SCHEMA IF NOT EXISTS crm;

CREATE TABLE crm.crm_config (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL UNIQUE,
    company_id BIGINT NOT NULL,
    CONSTRAINT fk_crm_config_company
            FOREIGN KEY (company_id)
            REFERENCES company.company (id)
);

CREATE TABLE crm.customer (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    customer_type VARCHAR(50) NOT NULL,
    company_name VARCHAR(256) NOT NULL,
    first_name VARCHAR(256) NOT NULL,
    last_name VARCHAR(256) NOT NULL,
    address_id BIGINT,
    phone_number_id BIGINT,
    email_id BIGINT,
    created_date TIMESTAMP NOT NULL,
    updated_date TIMESTAMP NOT NULL,
    created_by VARCHAR(256) NOT NULL,
    updated_by VARCHAR(256) NOT NULL,
    CONSTRAINT fk_native_crm_customer_crm_config
            FOREIGN KEY (tenant_id)
            REFERENCES crm.crm_config (tenant_id),
    CONSTRAINT fk_native_crm_customer_phone_number
            FOREIGN KEY (phone_number_id)
            REFERENCES base.phone_number (id),
    CONSTRAINT fk_native_crm_customer_address
            FOREIGN KEY (address_id)
            REFERENCES base.address (id),
    CONSTRAINT fk_native_crm_customer_email
            FOREIGN KEY (email_id)
            REFERENCES base.email (id)
);