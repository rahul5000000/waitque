CREATE TABLE company.company_email (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    email_id BIGINT NOT NULL UNIQUE,
    company_id BIGINT NOT NULL,
    CONSTRAINT fk_company_email_email
        FOREIGN KEY (email_id) REFERENCES base.email(id),
    CONSTRAINT fk_company_email_company
            FOREIGN KEY (company_id) REFERENCES company.company(id)
);