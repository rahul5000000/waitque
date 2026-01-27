package com.rrsgroup.customer.service.lead;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.common.dto.CompanyUserDto;
import com.rrsgroup.customer.domain.lead.LeadStatus;
import com.rrsgroup.customer.entity.lead.Lead;
import com.rrsgroup.customer.entity.lead.LeadStatusCount;
import com.rrsgroup.customer.repository.LeadRepository;
import com.rrsgroup.customer.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LeadService {
    private final LeadRepository leadRepository;
    private final EventService eventService;

    @Autowired
    public LeadService(LeadRepository leadRepository, EventService eventService) {
        this.leadRepository = leadRepository;
        this.eventService = eventService;
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

        Lead savedLead = leadRepository.save(lead);

        eventService.leadCreated(savedLead);

        return savedLead;
    }

    public Page<Lead> getCompanyListOfLeads(Long companyId, List<LeadStatus> statuses, int limit, int page, String sortField, SortDirection sortDir) {
        Pageable pageable = PageRequest.of(
                page,
                limit,
                sortDir == SortDirection.ASC ? Sort.by(sortField).ascending() : Sort.by(sortField).descending());

        if(statuses == null || statuses.isEmpty()) {
            return leadRepository.findByCompanyId(companyId, pageable);
        } else {
            return leadRepository.findByCompanyIdAndStatusIn(companyId, statuses, pageable);
        }
    }

    public Optional<Lead> getLeadById(Long id, CompanyUserDto userDto) {
        return leadRepository.findByIdAndCustomer_CrmConfig_CompanyId(id, userDto.getCompanyId());
    }

    public LeadStatusCount getLeadStatusCountForCompany(Long companyId) {
        return leadRepository.countLeadsByStatusForCompanyId(companyId);
    }
}
