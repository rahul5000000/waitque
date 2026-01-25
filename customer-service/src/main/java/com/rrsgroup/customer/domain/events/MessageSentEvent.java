package com.rrsgroup.customer.domain.events;

import com.rrsgroup.common.domain.events.EventType;
import com.rrsgroup.common.domain.events.WaitQueEvent;
import com.rrsgroup.customer.dto.message.MessageDto;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class MessageSentEvent extends WaitQueEvent {
    private MessageDto message;

    @Builder.Default
    private EventType eventType = EventType.MESSAGE_SENT;

    public MessageSentEvent() {
        this.setEventType(EventType.MESSAGE_SENT);
    }
}
