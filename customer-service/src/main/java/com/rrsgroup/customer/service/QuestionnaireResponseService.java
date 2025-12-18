package com.rrsgroup.customer.service;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.common.dto.CompanyUserDto;
import com.rrsgroup.common.dto.FieldUserDto;
import com.rrsgroup.customer.domain.questionnaireresponse.QuestionnaireResponseStatus;
import com.rrsgroup.customer.entity.questionnaireresponse.QuestionnaireResponse;
import com.rrsgroup.customer.entity.questionnaireresponse.QuestionnaireResponseAnswer;
import com.rrsgroup.customer.repository.QuestionnaireResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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

    public Optional<QuestionnaireResponse> getQuestionnaireResponseForCustomer(Long questionnaireResponseId, Long customerId, Long companyId) {
        return questionnaireResponseRepository.findByIdAndCustomer_IdAndCustomer_CrmConfig_CompanyId(questionnaireResponseId, customerId, companyId);
    }

    public QuestionnaireResponse updateQuestionnaireResponse(QuestionnaireResponse response, QuestionnaireResponse existingQuestionnaireResponse, FieldUserDto updatedByUser) {
        LocalDateTime now = LocalDateTime.now();
        String updatedBy = updatedByUser.getUserId();

        response.setPredecessor(existingQuestionnaireResponse);
        response.setCreatedBy(existingQuestionnaireResponse.getCreatedBy());
        response.setCreatedDate(existingQuestionnaireResponse.getCreatedDate());
        response.setUpdatedBy(updatedBy);
        response.setUpdatedDate(now);

        response.getAnswers().stream().filter(answer -> answer.getId() != null)
                .forEach(answer -> {
                    Optional<QuestionnaireResponseAnswer> existingAnswerOptional = existingQuestionnaireResponse.getAnswers().stream().filter(answer1 -> Objects.equals(answer1.getId(), answer.getId())).findFirst();

                    if(existingAnswerOptional.isPresent()) {
                        // This is an existing question
                        answer.setCreatedBy(existingAnswerOptional.get().getCreatedBy());
                        answer.setCreatedDate(existingAnswerOptional.get().getCreatedDate());
                        answer.setUpdatedBy(updatedBy);
                        answer.setUpdatedDate(now);
                    } else {
                        // This is an edge case where the question has an ID, but it doesn't match and existing question
                        answer.setCreatedBy(updatedByUser.getUserId());
                        answer.setCreatedDate(now);
                        answer.setUpdatedBy(updatedBy);
                        answer.setUpdatedDate(now);
                    }
                });
        // This is a new question
        response.getAnswers().stream().filter(answer -> answer.getId() == null)
                .forEach(answer -> {
                    answer.setCreatedBy(updatedByUser.getUserId());
                    answer.setCreatedDate(now);
                    answer.setUpdatedBy(updatedBy);
                    answer.setUpdatedDate(now);
                });

        existingQuestionnaireResponse.setStatus(QuestionnaireResponseStatus.INACTIVE);

        return questionnaireResponseRepository.saveAll(List.of(response, existingQuestionnaireResponse)).get(0);
    }

    public QuestionnaireResponse markQuestionnaireResponseStatus(
            QuestionnaireResponse existingQuestionnaireResponse, FieldUserDto updatedByUser,
            QuestionnaireResponseStatus newStatus) {
        LocalDateTime now = LocalDateTime.now();
        String updatedBy = updatedByUser.getUserId();

        existingQuestionnaireResponse.setStatus(newStatus);
        existingQuestionnaireResponse.setUpdatedBy(updatedBy);
        existingQuestionnaireResponse.setUpdatedDate(now);

        return questionnaireResponseRepository.save(existingQuestionnaireResponse);
    }
}
