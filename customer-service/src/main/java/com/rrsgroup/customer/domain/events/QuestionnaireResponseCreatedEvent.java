package com.rrsgroup.customer.domain.events;

import com.rrsgroup.common.domain.events.EventType;
import com.rrsgroup.common.domain.events.WaitQueEvent;
import com.rrsgroup.customer.dto.questionnaireresponse.QuestionnaireResponseDto;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class QuestionnaireResponseCreatedEvent extends WaitQueEvent {
    private QuestionnaireResponseDto questionnaireResponse;

    @Builder.Default
    private EventType eventType = EventType.QR_CREATED;

    public QuestionnaireResponseCreatedEvent() {
        this.setEventType(EventType.QR_CREATED);
    }
}
