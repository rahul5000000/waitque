ALTER TABLE company.lead_flow_order
DROP CONSTRAINT IF EXISTS uq_lead_flow_order_company_id_ordinal_key_status;

CREATE UNIQUE INDEX uq_lead_flow_order_company_id_ordinal_status_active
ON company.lead_flow_order (company_id, ordinal)
WHERE status = 'ACTIVE';