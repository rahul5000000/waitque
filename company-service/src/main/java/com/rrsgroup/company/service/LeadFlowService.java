package com.rrsgroup.company.service;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.common.dto.UserDto;
import com.rrsgroup.common.exception.IllegalUpdateException;
import com.rrsgroup.common.exception.RecordNotFoundException;
import com.rrsgroup.company.domain.LeadFlowStatus;
import com.rrsgroup.company.entity.Company;
import com.rrsgroup.company.entity.LeadFlow;
import com.rrsgroup.company.repository.LeadFlowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class LeadFlowService {
    private final Map<String, String> SQL_COLUMN_NAMESPACE_MAP = Map.of(
            "ordinal", "leadFlowOrder",
            "status", "leadFlowOrder"
        );

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

        setLeadFlowAuditFields(leadFlow, companyId, userId, now);

        return saveLeadFlowSafe(leadFlow);
    }

    private LeadFlow saveLeadFlowSafe(LeadFlow leadFlow) {
        try {
            return leadFlowRepository.save(leadFlow);
        } catch(DataIntegrityViolationException e) {
            // If the SQL unique constraint that prevents multiple active lead flows at the same ordinal was the violation
            if(e.getMessage().contains("uq_lead_flow_order_company_id_ordinal_status_active")) {
                throw new IllegalUpdateException("An active Lead Flow exists with the same ordinal");
            } else {
                // Otherwise it was some other violation, rethrow it because it's unhandled
                throw e;
            }
        }
    }

    private void setLeadFlowAuditFields(LeadFlow leadFlow, Long companyId, String createdByUserId, LocalDateTime createdDate) {
        setLeadFlowAuditFields(leadFlow, companyId, createdByUserId, createdDate, createdByUserId, createdDate, null);
    }

    private void setLeadFlowAuditFields(LeadFlow leadFlow, Long companyId, String createdByUserId, LocalDateTime createdDate, String updatedByUserId, LocalDateTime updatedDate, LeadFlow predecessor) {
        leadFlow.setCreatedBy(createdByUserId);
        leadFlow.setCreatedDate(createdDate);
        leadFlow.setUpdatedBy(updatedByUserId);
        leadFlow.setUpdatedDate(updatedDate);

        if(predecessor != null) {
            leadFlow.setPredecessor(predecessor);
        }

        leadFlow.getLeadFlowOrder().setCreatedBy(createdByUserId);
        leadFlow.getLeadFlowOrder().setCreatedDate(createdDate);
        leadFlow.getLeadFlowOrder().setUpdatedBy(updatedByUserId);
        leadFlow.getLeadFlowOrder().setUpdatedDate(updatedDate);
        leadFlow.getLeadFlowOrder().setCompany(getCompany(companyId));

        leadFlow.getQuestions().forEach(question -> {
            question.setCreatedBy(createdByUserId);
            question.setCreatedDate(createdDate);
            question.setUpdatedBy(updatedByUserId);
            question.setUpdatedDate(updatedDate);
        });
    }

    private Company getCompany(Long companyId) {
        Optional<Company> companyOptional = companyService.getCompany(companyId);

        if(companyOptional.isEmpty()) {
            throw new IllegalStateException("The company does not exist for companyId=" + companyId);
        }

        return companyOptional.get();
    }

    public Page<LeadFlow> getCompanyListOfLeadFlows(Long companyId, List<LeadFlowStatus> statuses, int limit, int page, String sortField, SortDirection sortDir) {
        String namespacedSortField = namespaceSortField(sortField);

        Pageable pageable = PageRequest.of(
                page,
                limit,
                sortDir == SortDirection.ASC ? Sort.by(namespacedSortField).ascending() : Sort.by(namespacedSortField).descending());

        if(statuses == null || statuses.isEmpty()) {
            return leadFlowRepository.findByCompanyId(companyId, pageable);
        } else {
            return leadFlowRepository.findByCompanyIdAndStatusIn(companyId, statuses, pageable);
        }
    }

    private String namespaceSortField(String sortField) {
        String namespace = SQL_COLUMN_NAMESPACE_MAP.get(sortField);

        if(namespace != null) {
            return namespace + "." + sortField;
        } else {
            return sortField;
        }
    }

    public LeadFlow getLeadFlow(Long leadFlowId, Long companyId) {
        return leadFlowRepository.findByIdAndCompanyId(leadFlowId, companyId);
    }

    @Transactional(rollbackFor = Exception.class)
    public LeadFlow updateLeadFlow(LeadFlow leadFlow, Long companyId, UserDto updatedBy) {
        LeadFlow existingLeadFlow = getLeadFlow(leadFlow.getId(), companyId);

        if(existingLeadFlow == null) {
            throw new RecordNotFoundException("The leadFlowId=" + leadFlow.getId() + " was not found");
        }

        LocalDateTime createdDate = existingLeadFlow.getCreatedDate();
        String createdByUserId = existingLeadFlow.getCreatedBy();
        LocalDateTime updatedDate = LocalDateTime.now();
        String updatedByUserId = updatedBy.getUserId();

        // Update existing lead flow to mark it inactive
        existingLeadFlow.getLeadFlowOrder().setStatus(LeadFlowStatus.INACTIVE);
        existingLeadFlow.setUpdatedBy(updatedByUserId);
        existingLeadFlow.setUpdatedDate(updatedDate);
        leadFlowRepository.saveAndFlush(existingLeadFlow);

        // Update new lead flow to set predecessor & fields to clone
        setLeadFlowAuditFields(leadFlow, companyId, createdByUserId, createdDate, updatedByUserId, updatedDate, existingLeadFlow);
        leadFlow.setId(null); // clear ID field so that a new record is created
        leadFlow.getQuestions().forEach(question -> question.setId(null)); // Safety, clear question ID field

        // Save new lead flow
        return saveLeadFlowSafe(leadFlow);
    }

    public LeadFlow inactivateLeadFlow(Long leadFlowId, Long companyId, UserDto updatedBy) {
        LeadFlow existingLeadFlow = getLeadFlow(leadFlowId, companyId);

        if(existingLeadFlow == null) {
            throw new RecordNotFoundException("The leadFlowId=" + leadFlowId + " was not found");
        }

        LocalDateTime updatedDate = LocalDateTime.now();
        String updatedByUserId = updatedBy.getUserId();

        existingLeadFlow.getLeadFlowOrder().setStatus(LeadFlowStatus.INACTIVE);
        existingLeadFlow.setUpdatedBy(updatedByUserId);
        existingLeadFlow.setUpdatedDate(updatedDate);
        return leadFlowRepository.saveAndFlush(existingLeadFlow);
    }
}
