CREATE SCHEMA IF NOT EXISTS customer;

CREATE TABLE customer.crm_config (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    crm_type VARCHAR(256) NOT NULL,
    crm_name VARCHAR(256) NOT NULL,
    created_date TIMESTAMP NOT NULL,
    updated_date TIMESTAMP NOT NULL,
    created_by VARCHAR(256) NOT NULL,
    updated_by VARCHAR(256) NOT NULL,
    CONSTRAINT fk_crm_config_company
        FOREIGN KEY (company_id)
        REFERENCES company.company (id)
);