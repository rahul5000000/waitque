package com.rrsgroup.company.service;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.common.dto.UserDto;
import com.rrsgroup.common.exception.IllegalUpdateException;
import com.rrsgroup.company.domain.Status;
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

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
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

    private Company getCompany(Long companyId) {
        Optional<Company> companyOptional = companyService.getCompany(companyId);

        if(companyOptional.isEmpty()) {
            throw new IllegalStateException("The company does not exist for companyId=" + companyId);
        }

        return companyOptional.get();
    }

    public Page<LeadFlow> getCompanyListOfLeadFlows(Long companyId, List<Status> statuses, int limit, int page, String sortField, SortDirection sortDir) {
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
}
