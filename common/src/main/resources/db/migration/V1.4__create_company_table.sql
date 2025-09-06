CREATE SCHEMA IF NOT EXISTS company;

CREATE TABLE company.company (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(512) NOT NULL,
    logoUrl VARCHAR(2048) NOT NULL,
    landingPrompt VARCHAR(512) NOT NULL,
    textColor VARCHAR(128) NOT NULL,
    backgroundColor VARCHAR(128) NOT NULL,
    primaryButtonColor VARCHAR(128) NOT NULL,
    secondaryButtonColor VARCHAR(128) NOT NULL,
    warningButtonColor VARCHAR(128) NOT NULL,
    dangerButtonColor VARCHAR(128) NOT NULL
);