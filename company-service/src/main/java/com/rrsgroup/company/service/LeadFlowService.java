package com.rrsgroup.company.service;

import com.rrsgroup.common.dto.UserDto;
import com.rrsgroup.company.entity.Company;
import com.rrsgroup.company.entity.LeadFlow;
import com.rrsgroup.company.repository.LeadFlowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LeadFlowService {
    private final LeadFlowRepository leadFlowRepository;
    private final CompanyService companyService;

    @Autowired
    public LeadFlowService(LeadFlowRepository leadFlowRepository, CompanyService companyService) {
        this.leadFlowRepository = leadFlowRepository;
        this.companyService = companyService;
    }

    public LeadFlow createLeadFlow(LeadFlow leadFlow, Long companyId, UserDto createdBy) {
        LocalDateTime now = LocalDateTime.now();
        String userId = createdBy.getUserId();

        leadFlow.setCreatedBy(userId);
        leadFlow.setCreatedDate(now);
        leadFlow.setUpdatedBy(userId);
        leadFlow.setUpdatedDate(now);

        leadFlow.getLeadFlowOrder().setCreatedBy(userId);
        leadFlow.getLeadFlowOrder().setCreatedDate(now);
        leadFlow.getLeadFlowOrder().setUpdatedBy(userId);
        leadFlow.getLeadFlowOrder().setUpdatedDate(now);
        leadFlow.getLeadFlowOrder().setCompany(getCompany(companyId));

        leadFlow.getQuestions().forEach(question -> {
            question.setCreatedBy(userId);
            question.setCreatedDate(now);
            question.setUpdatedBy(userId);
            question.setUpdatedDate(now);
        });

        return leadFlowRepository.save(leadFlow);
    }

    private Company getCompany(Long companyId) {
        Optional<Company> companyOptional = companyService.getCompany(companyId);

        if(companyOptional.isEmpty()) {
            throw new IllegalStateException("The company does not exist for companyId=" + companyId);
        }

        return companyOptional.get();
    }

}
