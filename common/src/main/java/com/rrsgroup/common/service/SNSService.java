package com.rrsgroup.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rrsgroup.common.domain.events.WaitQueEvent;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@Service
public class SNSService {
    private final SnsClient snsClient;
    private final SnsAsyncClient snsAsyncClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public SNSService(SnsClient snsClient, SnsAsyncClient snsAsyncClient, ObjectMapper objectMapper) {
        this.snsClient = snsClient;
        this.snsAsyncClient = snsAsyncClient;
        this.objectMapper = objectMapper;
    }

    public void publishEvent(String topicArn, WaitQueEvent event) {
        publishEvent(topicArn, event, false);
    }

    public void publishEventAsync(String topicArn, WaitQueEvent event) {
        publishEvent(topicArn, event, true);
    }

    private void publishEvent(String topicArn, WaitQueEvent event, boolean async) {
        String message = toJson(event);

        PublishRequest request = PublishRequest.builder()
                .topicArn(topicArn)
                .message(message)
                .messageAttributes(buildMessageAttributes(event))
                .build();

        if(async) {
            snsAsyncClient.publish(request)
                    .whenComplete((response, error) -> {
                        if (error != null) {
                            log.warn("Async SNS publish failed (ignored)", error);
                        }
                    });
        } else {
            snsClient.publish(request);
        }
    }

    private Map<String, MessageAttributeValue> buildMessageAttributes(WaitQueEvent event) {
        Map<String, MessageAttributeValue> map = new HashMap<>();
        map.put("eventType", MessageAttributeValue.builder().dataType("String").stringValue(event.getEventType().getType()).build());

        if(event.getCompanyId() != null) {
            map.put("companyId", MessageAttributeValue.builder().dataType("String").stringValue(event.getCompanyId().toString()).build());
        }

        if(event.getCustomerId() != null) {
            map.put("customerId", MessageAttributeValue.builder().dataType("String").stringValue(event.getCustomerId().toString()).build());
        }

        if(event.getUserId() != null) {
            map.put("userId", MessageAttributeValue.builder().dataType("String").stringValue(event.getUserId()).build());
        }

        return map;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize SNS message", e);
        }
    }
}
