ALTER TABLE company.lead_flow
ADD COLUMN predecessor_id BIGINT UNIQUE,
ADD CONSTRAINT fk_leadflow_predecessor FOREIGN KEY (predecessor_id) REFERENCES company.lead_flow(id);