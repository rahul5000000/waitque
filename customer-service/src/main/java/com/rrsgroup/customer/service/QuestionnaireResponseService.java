package com.rrsgroup.customer.service;

import com.rrsgroup.common.dto.FieldUserDto;
import com.rrsgroup.customer.domain.questionnaire.QuestionnaireStatus;
import com.rrsgroup.customer.entity.questionnaireresponse.QuestionnaireResponse;
import com.rrsgroup.customer.repository.QuestionnaireResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
}
