
CREATE TABLE crm.customer_phone_number (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(256) NOT NULL,
    phone_number_id BIGINT,
    crm_customer_id BIGINT,
    created_date TIMESTAMP NOT NULL,
    updated_date TIMESTAMP NOT NULL,
    created_by VARCHAR(256) NOT NULL,
    updated_by VARCHAR(256) NOT NULL,
    CONSTRAINT fk_native_crm_customer_phone_number_phone_number
            FOREIGN KEY (phone_number_id)
            REFERENCES base.phone_number (id),
    CONSTRAINT fk_native_crm_customer_phone_number_native_crm_customer
            FOREIGN KEY (crm_customer_id)
            REFERENCES crm.customer (id)
);