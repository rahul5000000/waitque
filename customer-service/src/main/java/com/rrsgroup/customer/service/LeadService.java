package com.rrsgroup.customer.service;

import com.rrsgroup.customer.domain.lead.LeadStatus;
import com.rrsgroup.customer.entity.lead.Lead;
import com.rrsgroup.customer.repository.LeadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LeadService {
    private final LeadRepository leadRepository;

    @Autowired
    public LeadService(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    public Lead createLeadAnonymous(Lead lead) {
        LocalDateTime now = LocalDateTime.now();
        String createdBy = "anonymous";

        lead.setStatus(LeadStatus.NEW);
        lead.setCreatedDate(now);
        lead.setUpdatedDate(now);
        lead.setCreatedBy(createdBy);
        lead.setUpdatedBy(createdBy);

        lead.getAnswers().stream().forEach(answer -> {
            answer.setCreatedDate(now);
            answer.setUpdatedDate(now);
            answer.setCreatedBy(createdBy);
            answer.setUpdatedBy(createdBy);
        });

        return leadRepository.save(lead);
    }
}
