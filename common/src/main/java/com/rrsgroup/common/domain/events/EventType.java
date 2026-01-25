package com.rrsgroup.common.domain.events;

public enum EventType {
    LEAD_CREATED("LEAD_CREATED"),
    MESSAGE_SENT("MESSAGE_SENT"),
    CUSTOMER_LOGIN("CUSTOMER_LOGIN"),
    QR_CREATED("QR_CREATED"),
    QR_VIEWED("QR_VIEWED");

    private final String eventType;

    private EventType(String eventType) {
        this.eventType = eventType;
    }

    public String getType() {
        return eventType;
    }
}
