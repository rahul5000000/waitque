package com.rrsgroup.customer.service;

import com.rrsgroup.common.service.SNSService;
import com.rrsgroup.customer.domain.events.LeadCreatedEvent;
import com.rrsgroup.customer.entity.lead.Lead;
import com.rrsgroup.customer.service.lead.LeadDtoMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class EventService {
    @Value("${sns.topics.lead-created}")
    private String leadCreatedTopicArn;

    private final SNSService snsService;
    private final LeadDtoMapper leadDtoMapper;

    @Autowired
    public EventService(SNSService snsService, LeadDtoMapper leadDtoMapper) {
        this.snsService = snsService;
        this.leadDtoMapper = leadDtoMapper;
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
}
