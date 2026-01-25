package com.rrsgroup.customer.service;

import com.rrsgroup.common.dto.UserDto;
import com.rrsgroup.common.service.SNSService;
import com.rrsgroup.customer.domain.events.*;
import com.rrsgroup.customer.dto.questionnaireresponse.QuestionnaireResponseDto;
import com.rrsgroup.customer.entity.Customer;
import com.rrsgroup.customer.entity.lead.Lead;
import com.rrsgroup.customer.entity.message.Message;
import com.rrsgroup.customer.service.lead.LeadDtoMapper;
import com.rrsgroup.customer.service.message.MessageDtoMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Log4j2
@Service
public class EventService {
    @Value("${sns.topics.lead-created}")
    private String leadCreatedTopicArn;

    @Value("${sns.topics.message-sent}")
    private String messageSentTopicArn;

    @Value("${sns.topics.customer-login}")
    private String customerLoginTopicArn;

    @Value("${sns.topics.qr-created}")
    private String qrCreatedTopicArn;

    @Value("${sns.topics.qr-viewed}")
    private String qrViewedTopicArn;

    private final SNSService snsService;
    private final LeadDtoMapper leadDtoMapper;
    private final MessageDtoMapper messageDtoMapper;

    @Autowired
    public EventService(SNSService snsService, LeadDtoMapper leadDtoMapper, MessageDtoMapper messageDtoMapper) {
        this.snsService = snsService;
        this.leadDtoMapper = leadDtoMapper;
        this.messageDtoMapper = messageDtoMapper;
    }

    public void leadCreated(Lead lead) {
        try {
            LeadCreatedEvent event = LeadCreatedEvent.builder()
                    .lead(leadDtoMapper.map(lead))
                    .companyId(lead.getCustomer().getCrmConfig().getCompanyId())
                    .customerId(lead.getCustomer().getId())
                    .userId(lead.getCreatedBy())
                    .build();

            snsService.publishEventAsync(leadCreatedTopicArn, event);
        } catch (Exception e) {
            log.warn("Error publishing event to SNS", e);
        }
    }

    public void messageSent(Message message) {
        try {
            MessageSentEvent event = MessageSentEvent.builder()
                    .message(messageDtoMapper.map(message))
                    .companyId(message.getCustomer().getCrmConfig().getCompanyId())
                    .customerId(message.getCustomer().getId())
                    .userId(message.getCreatedBy())
                    .build();

            snsService.publishEventAsync(messageSentTopicArn, event);
        } catch (Exception e) {
            log.warn("Error publishing event to SNS", e);
        }
    }

    public void customerLogin(Customer customer, UUID qrCode) {
        try {
            CustomerLoginEvent event = CustomerLoginEvent.builder()
                    .qrCode(qrCode)
                    .companyId(customer.getCrmConfig().getCompanyId())
                    .customerId(customer.getId())
                    .build();

            snsService.publishEventAsync(customerLoginTopicArn, event);
        } catch (Exception e) {
            log.warn("Error publishing event to SNS", e);
        }
    }

    public void questionnaireResponseCreated(QuestionnaireResponseDto qr, Customer customer, UserDto user) {
        try {
            QuestionnaireResponseCreatedEvent event = QuestionnaireResponseCreatedEvent.builder()
                    .questionnaireResponse(qr)
                    .companyId(customer.getCrmConfig().getCompanyId())
                    .customerId(customer.getId())
                    .userId(user.getUserId())
                    .build();

            snsService.publishEventAsync(qrCreatedTopicArn, event);
        } catch (Exception e) {
            log.warn("Error publishing event to SNS", e);
        }
    }

    public void questionnaireResponseViewed(QuestionnaireResponseDto qr, UUID qrCode, Customer customer) {
        try {
            QuestionnaireResponseViewedEvent event = QuestionnaireResponseViewedEvent.builder()
                    .questionnaireResponse(qr)
                    .qrCode(qrCode)
                    .companyId(customer.getCrmConfig().getCompanyId())
                    .customerId(customer.getId())
                    .build();

            snsService.publishEventAsync(qrViewedTopicArn, event);
        } catch (Exception e) {
            log.warn("Error publishing event to SNS", e);
        }
    }
}
