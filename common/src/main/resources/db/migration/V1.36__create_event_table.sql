CREATE SCHEMA IF NOT EXISTS event;

CREATE TABLE event.generic_event (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Event classification
    event_type TEXT NOT NULL,
    event_version INTEGER NOT NULL DEFAULT 1,

    company_id BIGINT,

    -- SNS / idempotency support
    source TEXT NOT NULL,                      -- e.g. backend, mobile, keycloak
    external_event_id TEXT,           -- SNS message ID for deduplication

    -- Event payload
    payload JSONB NOT NULL,

    -- Timing
    occurred_at TIMESTAMPTZ NOT NULL, -- when event actually happened
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
