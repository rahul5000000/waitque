package com.rrsgroup.common.domain.events;

public enum EventType {
    LEAD_CREATED("LEAD_CREATED");

    private final String eventType;

    private EventType(String eventType) {
        this.eventType = eventType;
    }

    public String getType() {
        return eventType;
    }
}
