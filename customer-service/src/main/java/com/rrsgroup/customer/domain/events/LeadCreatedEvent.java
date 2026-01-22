package com.rrsgroup.customer.domain.events;

import com.rrsgroup.common.domain.events.EventType;
import com.rrsgroup.common.domain.events.WaitQueEvent;
import com.rrsgroup.customer.dto.lead.LeadDto;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class LeadCreatedEvent extends WaitQueEvent {
    private LeadDto lead;

    @Builder.Default
    private EventType eventType = EventType.LEAD_CREATED;

    public LeadCreatedEvent() {
        this.setEventType(EventType.LEAD_CREATED);
    }
}
