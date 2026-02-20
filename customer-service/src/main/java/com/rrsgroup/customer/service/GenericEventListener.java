package com.rrsgroup.customer.service;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class GenericEventListener {
    @SqsListener(value = "${sqs.queue-names.qr-viewed}")
    public void handleMessage(String message) {
        log.info("Received message from SQS: {}", message);
        // process + write to DB
    }
}
