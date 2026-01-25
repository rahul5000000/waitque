package com.rrsgroup.customer.domain.events;

import com.rrsgroup.common.domain.events.EventType;
import com.rrsgroup.common.domain.events.WaitQueEvent;
import com.rrsgroup.customer.dto.questionnaireresponse.QuestionnaireResponseDto;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
public class QuestionnaireResponseViewedEvent extends WaitQueEvent {
    private QuestionnaireResponseDto questionnaireResponse;
    private UUID qrCode;

    @Builder.Default
    private EventType eventType = EventType.QR_VIEWED;

    public QuestionnaireResponseViewedEvent() {
        this.setEventType(EventType.QR_VIEWED);
    }
}
