ALTER TABLE company.company
ADD COLUMN address_id BIGINT NOT NULL UNIQUE,
ADD CONSTRAINT fk_company_address
    FOREIGN KEY (address_id) REFERENCES base.address(id);

ALTER TABLE company.company
ADD COLUMN phone_number_id BIGINT NOT NULL UNIQUE,
ADD CONSTRAINT fk_company_phone_number
    FOREIGN KEY (phone_number_id) REFERENCES base.phone_number(id);