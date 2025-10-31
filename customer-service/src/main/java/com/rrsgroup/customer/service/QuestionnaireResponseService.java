package com.rrsgroup.customer.service;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.common.dto.CompanyUserDto;
import com.rrsgroup.common.dto.FieldUserDto;
import com.rrsgroup.customer.domain.lead.LeadStatus;
import com.rrsgroup.customer.domain.questionnaire.QuestionnaireStatus;
import com.rrsgroup.customer.domain.questionnaireresponse.QuestionnaireResponseStatus;
import com.rrsgroup.customer.entity.lead.Lead;
import com.rrsgroup.customer.entity.questionnaireresponse.QuestionnaireResponse;
import com.rrsgroup.customer.repository.QuestionnaireResponseRepository;
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
public class QuestionnaireResponseService {
    private final QuestionnaireResponseRepository questionnaireResponseRepository;

    @Autowired
    public QuestionnaireResponseService(QuestionnaireResponseRepository questionnaireResponseRepository) {
        this.questionnaireResponseRepository = questionnaireResponseRepository;
    }

    public QuestionnaireResponse createQuestionnaireResponse(QuestionnaireResponse response, FieldUserDto createdByUser) {
        LocalDateTime now = LocalDateTime.now();
        String createdBy = createdByUser.getUserId();

        response.setCreatedBy(createdBy);
        response.setUpdatedBy(createdBy);
        response.setCreatedDate(now);
        response.setUpdatedDate(now);

        response.getAnswers().stream().forEach(answer -> {
            answer.setCreatedBy(createdBy);
            answer.setUpdatedBy(createdBy);
            answer.setCreatedDate(now);
            answer.setUpdatedDate(now);
        });

        return questionnaireResponseRepository.save(response);
    }

    public Page<QuestionnaireResponse> getCustomerListOfQuestionnaireResponses(Long customerId, Long companyId, List<QuestionnaireResponseStatus> statuses, int limit, int page, String sortField, SortDirection sortDir) {
        Pageable pageable = PageRequest.of(
                page,
                limit,
                sortDir == SortDirection.ASC ? Sort.by(sortField).ascending() : Sort.by(sortField).descending());

        if(statuses == null || statuses.isEmpty()) {
            return questionnaireResponseRepository.findByCustomerIdAndCompanyId(customerId, companyId, pageable);
        } else {
            return questionnaireResponseRepository.findByCustomerIdAndCompanyIdAndStatusIn(customerId, companyId, statuses, pageable);
        }
    }

    public Optional<QuestionnaireResponse> getQuestionnaireResponseForCustomer(Long questionnaireResponseId, Long customerId, CompanyUserDto userDto) {
        return questionnaireResponseRepository.findByIdAndCustomer_IdAndCustomer_CrmConfig_CompanyId(questionnaireResponseId, customerId, userDto.getCompanyId());
    }
}
