CREATE TABLE company.qr_code (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    qr_code UUID NOT NULL,
    UNIQUE(company_id, qr_code),
    created_date TIMESTAMP NOT NULL,
    updated_date TIMESTAMP NOT NULL,
    created_by VARCHAR(256) NOT NULL,
    updated_by VARCHAR(256) NOT NULL,
    CONSTRAINT fk_qr_code_usage_company
            FOREIGN KEY (company_id) REFERENCES company.company(id)
);