CREATE TABLE company.lead_flow (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(512) NOT NULL,
    name VARCHAR(512) NOT NULL,
    icon VARCHAR(2048) NOT NULL,
    button_text VARCHAR(128) NOT NULL,
    title VARCHAR(128) NOT NULL,
    confirmation_message_header VARCHAR(128) NOT NULL,
    confirmation_message_1 VARCHAR(128),
    confirmation_message_2 VARCHAR(128),
    confirmation_message_3 VARCHAR(128),
    created_date TIMESTAMP NOT NULL,
    updated_date TIMESTAMP NOT NULL,
    created_by VARCHAR(256) NOT NULL,
    updated_by VARCHAR(256) NOT NULL
);

CREATE INDEX idx_lead_flow_status_id
    ON company.lead_flow (status, id);