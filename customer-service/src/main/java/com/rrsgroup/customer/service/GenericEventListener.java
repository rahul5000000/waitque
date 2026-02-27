package com.rrsgroup.customer.service;

import com.rrsgroup.common.domain.events.EventType;
import com.rrsgroup.customer.domain.events.QuestionnaireResponseViewedEvent;
import com.rrsgroup.customer.entity.event.GenericEvent;
import com.rrsgroup.customer.repository.GenericEventRepository;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Log4j2
public class GenericEventListener {
    private final GenericEventRepository repository;

    @Autowired
    public GenericEventListener(GenericEventRepository repository) {
        this.repository = repository;
    }

    @SqsListener(value = "${sqs.queue-names.qr-viewed}")
    public void handleMessage(Message<Map<String, Object>> snsMessage) {
        log.info("Received message from SQS: {}", snsMessage);

        Map<String, Object> message = snsMessage.getPayload();

        UUID externalEventId = (UUID) snsMessage.getHeaders().get("id");
        EventType eventType = EventType.valueOf((String) message.get("eventType"));
        Long companyId = Long.valueOf((Integer) message.get("companyId"));
        String source = "SQS";
        Map<String, Object> payload;
        LocalDateTime occurredAt = LocalDateTime.parse((String) message.get("occurredAt"));
        LocalDateTime createdAt = LocalDateTime.now();

        if(eventType == EventType.QR_VIEWED) {
            log.info("Processing QuestionnaireResponseViewedEvent for companyId: {}", companyId);
            payload = new HashMap<>();
            payload.put("qrId", message.get("qrId"));
        }
        // Which QR
        // QR code who viewed the QR

        GenericEvent genericEvent = GenericEvent.builder()
                .externalEventId(externalEventId.toString())
                .eventType(eventType)
                .companyId(companyId)
                .source(source)
                .payload(message)
                .occurredAt(occurredAt)
                .createdAt(createdAt)
                .build();

        repository.save(genericEvent);
        // process + write to DB
    }
}
