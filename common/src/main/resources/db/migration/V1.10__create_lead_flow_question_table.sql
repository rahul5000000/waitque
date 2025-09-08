CREATE TABLE company.lead_flow_question (
    id BIGSERIAL PRIMARY KEY,
    lead_flow_id BIGINT NOT NULL,
    question VARCHAR(512) NOT NULL,
    data_type VARCHAR(512) NOT NULL,
    created_date TIMESTAMP NOT NULL,
    updated_date TIMESTAMP NOT NULL,
    created_by VARCHAR(256) NOT NULL,
    updated_by VARCHAR(256) NOT NULL,
    CONSTRAINT fk_lead_flow_question_lead_flow
                FOREIGN KEY (lead_flow_id) REFERENCES company.lead_flow(id)
);

CREATE INDEX idx_lead_flow_question_lead_flow_id
    ON company.lead_flow_question (lead_flow_id);