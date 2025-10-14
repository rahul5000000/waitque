CREATE TABLE customer.lead (
    id BIGSERIAL PRIMARY KEY,
    lead_flow_id BIGINT NOT NULL,
    status VARCHAR(256) NOT NULL,
    customer_id BIGINT NOT NULL,
    override_first_name VARCHAR(256),
    override_last_name VARCHAR(256),
    override_address_id BIGINT,
    override_phone_number_id BIGINT,
    override_email VARCHAR(512),
    created_date TIMESTAMP NOT NULL,
    updated_date TIMESTAMP NOT NULL,
    created_by VARCHAR(256) NOT NULL,
    updated_by VARCHAR(256) NOT NULL,
    CONSTRAINT fk_lead_lead_flow
        FOREIGN KEY (lead_flow_id)
        REFERENCES company.lead_flow (id),
    CONSTRAINT fk_lead_customer
            FOREIGN KEY (customer_id)
            REFERENCES customer.customer (id),
    CONSTRAINT fk_lead_phone_number
                FOREIGN KEY (override_phone_number_id)
                REFERENCES base.phone_number (id)
);

CREATE TABLE customer.lead_answer (
    id BIGSERIAL PRIMARY KEY,
    lead_id BIGINT NOT NULL,
    lead_flow_question_id BIGINT NOT NULL,
    dataType VARCHAR(256) NOT NULL,
    boolean_answer BOOLEAN,
    text_answer VARCHAR(512),
    text_area_answer VARCHAR(2048),
    image_url VARCHAR(2048),
    number_answer BIGINT,
    decimal_answer DOUBLE PRECISION,
    created_date TIMESTAMP NOT NULL,
    updated_date TIMESTAMP NOT NULL,
    created_by VARCHAR(256) NOT NULL,
    updated_by VARCHAR(256) NOT NULL,
    CONSTRAINT fk_lead_answer_lead
        FOREIGN KEY (lead_id)
        REFERENCES customer.lead (id),
    CONSTRAINT fk_lead_answer_lead_flow_question
            FOREIGN KEY (lead_flow_question_id)
            REFERENCES company.lead_flow_question (id)
);