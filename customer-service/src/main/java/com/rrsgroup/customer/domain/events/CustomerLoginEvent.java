package com.rrsgroup.customer.domain.events;

import com.rrsgroup.common.domain.events.EventType;
import com.rrsgroup.common.domain.events.WaitQueEvent;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
public class CustomerLoginEvent extends WaitQueEvent {
    private UUID qrCode;

    @Builder.Default
    private EventType eventType = EventType.CUSTOMER_LOGIN;

    public CustomerLoginEvent() {
        this.setEventType(EventType.CUSTOMER_LOGIN);
    }
}
