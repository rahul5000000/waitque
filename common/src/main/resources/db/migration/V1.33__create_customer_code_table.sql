CREATE TABLE customer.customer_code (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT,
    customer_code VARCHAR(8) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    created_date TIMESTAMP NOT NULL,
    updated_date TIMESTAMP NOT NULL,
    created_by VARCHAR(256) NOT NULL,
    updated_by VARCHAR(256) NOT NULL,
    CONSTRAINT fk_customer_code_customer
                FOREIGN KEY (customer_id) REFERENCES customer.customer(id)
);