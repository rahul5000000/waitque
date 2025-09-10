ALTER TABLE company.lead_flow
DROP COLUMN status;

ALTER TABLE company.lead_flow_order
ADD COLUMN status VARCHAR(512) NOT NULL;

ALTER TABLE company.lead_flow_order
DROP CONSTRAINT lead_flow_order_company_id_ordinal_key;

ALTER TABLE company.lead_flow_order
ADD CONSTRAINT uq_lead_flow_order_company_id_ordinal_key_status
UNIQUE(company_id, ordinal, status);