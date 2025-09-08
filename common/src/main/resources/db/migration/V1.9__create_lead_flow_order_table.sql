CREATE TABLE company.lead_flow_order (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    lead_flow_id BIGINT NOT NULL,
    ordinal INT NOT NULL,
    UNIQUE(company_id, ordinal),
    created_date TIMESTAMP NOT NULL,
    updated_date TIMESTAMP NOT NULL,
    created_by VARCHAR(256) NOT NULL,
    updated_by VARCHAR(256) NOT NULL,
    CONSTRAINT fk_lead_flow_order_company
            FOREIGN KEY (company_id) REFERENCES company.company(id),
    CONSTRAINT fk_lead_flow_order_lead_flow
                FOREIGN KEY (lead_flow_id) REFERENCES company.lead_flow(id)
);

CREATE INDEX idx_lead_flow_order_company
    ON company.lead_flow_order (company_id);