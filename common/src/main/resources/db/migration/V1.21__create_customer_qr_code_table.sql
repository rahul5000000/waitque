DROP TABLE company.qr_code;

CREATE TABLE customer.qr_code (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    customer_id BIGINT,
    qr_code UUID NOT NULL,
    UNIQUE(company_id, qr_code),
    created_date TIMESTAMP NOT NULL,
    updated_date TIMESTAMP NOT NULL,
    created_by VARCHAR(256) NOT NULL,
    updated_by VARCHAR(256) NOT NULL,
    CONSTRAINT fk_qr_code_company
            FOREIGN KEY (company_id) REFERENCES company.company(id),
    CONSTRAINT fk_qr_code_customer
                FOREIGN KEY (customer_id) REFERENCES customer.customer(id)
);