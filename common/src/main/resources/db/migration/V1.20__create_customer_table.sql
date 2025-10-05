CREATE TABLE customer.customer (
    id BIGSERIAL PRIMARY KEY,
    crm_config_id BIGINT NOT NULL,
    crm_customer_id VARCHAR(256) NOT NULL,
    created_date TIMESTAMP NOT NULL,
    updated_date TIMESTAMP NOT NULL,
    created_by VARCHAR(256) NOT NULL,
    updated_by VARCHAR(256) NOT NULL,
    CONSTRAINT fk_customer_crm_config
        FOREIGN KEY (crm_config_id)
        REFERENCES customer.crm_config (id)
);