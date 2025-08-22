ALTER TABLE company
ADD COLUMN address_id BIGINT NOT NULL UNIQUE,
ADD CONSTRAINT fk_company_address
    FOREIGN KEY (address_id) REFERENCES address(id);

ALTER TABLE company
ADD COLUMN phone_number_id BIGINT NOT NULL UNIQUE,
ADD CONSTRAINT fk_company_phone_number
    FOREIGN KEY (phone_number_id) REFERENCES phone_number(id);