CREATE TABLE customer.message (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(256) NOT NULL,
    customer_id BIGINT NOT NULL,
    override_first_name VARCHAR(256),
    override_last_name VARCHAR(256),
    override_address_id BIGINT,
    override_phone_number_id BIGINT,
    override_email VARCHAR(512),
    message VARCHAR(2048) NOT NULL,
    created_date TIMESTAMP NOT NULL,
    updated_date TIMESTAMP NOT NULL,
    created_by VARCHAR(256) NOT NULL,
    updated_by VARCHAR(256) NOT NULL,
    CONSTRAINT fk_message_customer
            FOREIGN KEY (customer_id)
            REFERENCES customer.customer (id),
    CONSTRAINT fk_message_phone_number
                FOREIGN KEY (override_phone_number_id)
                REFERENCES base.phone_number (id),
    CONSTRAINT fk_message_address
                    FOREIGN KEY (override_address_id)
                    REFERENCES base.address (id)
);

ALTER TABLE customer.lead
ADD CONSTRAINT fk_lead_address
FOREIGN KEY (override_address_id)
REFERENCES base.address (id);
