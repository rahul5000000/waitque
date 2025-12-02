package com.rrsgroup.common.service;

import com.rrsgroup.common.EmailRequest;
import com.rrsgroup.common.domain.EmailTemplate;
import com.rrsgroup.common.dto.UserDto;
import com.rrsgroup.common.entity.EmailHistory;
import com.rrsgroup.common.exception.EmailSendException;
import com.rrsgroup.common.repository.EmailHistoryRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@Log4j2
public class EmailService {
    private final SesClient sesClient;
    private final EmailHistoryRepository emailHistoryRepository;
    private final TemplateEngine templateEngine;

    @Autowired
    public EmailService(
            SesClient sesClient,
            EmailHistoryRepository emailHistoryRepository,
            TemplateEngine templateEngine) {
        this.sesClient = sesClient;
        this.emailHistoryRepository = emailHistoryRepository;
        this.templateEngine = templateEngine;
    }

    public void sendEmail(EmailRequest sendEmailRequest) throws EmailSendException {
        sendEmail(sendEmailRequest, null);
    }

    private void sendEmail(EmailRequest sendEmailRequest, UserDto user) throws EmailSendException {
        // TODO: Convert sending emails to an async process
        try {
            SendEmailRequest request = SendEmailRequest.builder()
                    .source("rahul@therrsgroup.com")
                    .destination(Destination.builder()
                            .toAddresses(sendEmailRequest.toEmail().getEmail())
                            .build())
                    .message(Message.builder()
                            .subject(Content.builder()
                                    .data(sendEmailRequest.template().getSubject())
                                    .build())
                            .body(Body.builder()
                                    .html(Content.builder()
                                            .data(sendEmailRequest.htmlBody())
                                            .build())
                                    .build())
                            .build())
                    .build();

            LocalDateTime now = LocalDateTime.now();
            String createdByUserId = user != null ? user.getUserId() : "anonymous";
            EmailHistory history = EmailHistory.builder().email(sendEmailRequest.toEmail()).sendTime(now)
                    .subject(sendEmailRequest.template().getSubject()).content(sendEmailRequest.htmlBody()).createdDate(now)
                    .updatedDate(now).createdBy(createdByUserId).updatedBy(createdByUserId).build();
            emailHistoryRepository.save(history);

            sesClient.sendEmail(request);
        } catch (Exception e) {
            throw new EmailSendException("Failed to send email", e);
        }
    }

    public String render(EmailTemplate template, Map<String, Object> model) {
        Context ctx = new Context();

        if (model != null) {
            model.forEach(ctx::setVariable);
        }

        // Render the base layout
        return templateEngine.process(template.getTemplateName(), ctx);
    }
}
