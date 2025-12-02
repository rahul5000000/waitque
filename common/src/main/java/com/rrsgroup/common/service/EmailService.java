package com.rrsgroup.common.service;

import com.rrsgroup.common.exception.EmailSendException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
@Log4j2
public class EmailService {
    private SesClient sesClient;

    @Autowired
    public EmailService(SesClient sesClient) {
        this.sesClient = sesClient;
    }

    public void sendEmail(String message, String toEmail) throws EmailSendException {
        // TODO: Convert sending emails to an async process
        try {
            SendEmailRequest request = SendEmailRequest.builder()
                    .source("rahul@therrsgroup.com")
                    .destination(Destination.builder()
                            .toAddresses(toEmail)
                            .build())
                    .message(Message.builder()
                            .subject(Content.builder()
                                    .data("Hello from SES")
                                    .build())
                            .body(Body.builder()
                                    .text(Content.builder()
                                            .data(message)
                                            .build())
                                    .build())
                            .build())
                    .build();

            sesClient.sendEmail(request);
        } catch (Exception e) {
            throw new EmailSendException("Failed to send email", e);
        }
    }
}
