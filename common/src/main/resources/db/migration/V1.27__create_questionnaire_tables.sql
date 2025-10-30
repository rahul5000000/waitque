CREATE TABLE company.questionnaire (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    name VARCHAR(512) NOT NULL,
    description VARCHAR(2056) NOT NULL,
    status VARCHAR(512) NOT NULL,
    predecessor_id BIGINT UNIQUE,
    created_date TIMESTAMP NOT NULL,
    updated_date TIMESTAMP NOT NULL,
    created_by VARCHAR(256) NOT NULL,
    updated_by VARCHAR(256) NOT NULL,
    CONSTRAINT fk_questionnaire_company
            FOREIGN KEY (company_id) REFERENCES company.company(id),
    CONSTRAINT fk_questionnaire_predecessor
                FOREIGN KEY (predecessor_id) REFERENCES company.lead_flow(id)
);

CREATE INDEX idx_questionnaire_company_status
    ON company.questionnaire (company_id, status);

CREATE TABLE company.questionnaire_page (
    id BIGSERIAL PRIMARY KEY,
    questionnaire_id BIGINT NOT NULL,
    page_title VARCHAR(512) NOT NULL,
    page_number INT NOT NULL,
    UNIQUE(questionnaire_id, page_number),
    created_date TIMESTAMP NOT NULL,
    updated_date TIMESTAMP NOT NULL,
    created_by VARCHAR(256) NOT NULL,
    updated_by VARCHAR(256) NOT NULL,
    CONSTRAINT fk_questionnaire_page_questionnaire
            FOREIGN KEY (questionnaire_id) REFERENCES company.questionnaire(id)
);

CREATE TABLE company.questionnaire_question (
    id BIGSERIAL PRIMARY KEY,
    questionnaire_page_id BIGINT NOT NULL,
    question VARCHAR(512) NOT NULL,
    data_type VARCHAR(512) NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT FALSE,
    false_text VARCHAR(512),
    true_text VARCHAR(512),
    question_group VARCHAR(256),
    created_date TIMESTAMP NOT NULL,
    updated_date TIMESTAMP NOT NULL,
    created_by VARCHAR(256) NOT NULL,
    updated_by VARCHAR(256) NOT NULL,
    CONSTRAINT fk_questionnaire_question_questionnaire_page
            FOREIGN KEY (questionnaire_page_id) REFERENCES company.questionnaire_page(id)
);