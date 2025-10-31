CREATE TABLE customer.questionnaire_response (
    id BIGSERIAL PRIMARY KEY,
    questionnaire_id BIGINT NOT NULL,
    status VARCHAR(256) NOT NULL,
    customer_id BIGINT NOT NULL,
    created_date TIMESTAMP NOT NULL,
    updated_date TIMESTAMP NOT NULL,
    created_by VARCHAR(256) NOT NULL,
    updated_by VARCHAR(256) NOT NULL,
    CONSTRAINT fk_questionnaire_response_questionnaire
        FOREIGN KEY (questionnaire_id)
        REFERENCES company.questionnaire (id),
    CONSTRAINT fk_questionnaire_response_customer
            FOREIGN KEY (customer_id)
            REFERENCES customer.customer (id)
);

CREATE TABLE customer.questionnaire_response_answer (
    id BIGSERIAL PRIMARY KEY,
    questionnaire_response_id BIGINT NOT NULL,
    questionnaire_question_id BIGINT NOT NULL,
    data_type VARCHAR(256) NOT NULL,
    boolean_answer BOOLEAN,
    text_answer VARCHAR(512),
    text_area_answer VARCHAR(2048),
    image_url VARCHAR(2048),
    number_answer BIGINT,
    decimal_answer DOUBLE PRECISION,
    phone_answer BIGINT,
    email_answer VARCHAR(256),
    created_date TIMESTAMP NOT NULL,
    updated_date TIMESTAMP NOT NULL,
    created_by VARCHAR(256) NOT NULL,
    updated_by VARCHAR(256) NOT NULL,
    CONSTRAINT fk_questionnaire_response_answer_questionnaire_response
        FOREIGN KEY (questionnaire_response_id)
        REFERENCES customer.questionnaire_response (id),
    CONSTRAINT fk_questionnaire_response_answer_questionnaire_question
            FOREIGN KEY (questionnaire_question_id)
            REFERENCES company.questionnaire_question (id)
);