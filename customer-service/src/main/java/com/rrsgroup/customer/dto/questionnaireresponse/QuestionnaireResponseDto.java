package com.rrsgroup.customer.dto.questionnaireresponse;

import com.rrsgroup.customer.domain.CrmCustomer;
import com.rrsgroup.customer.domain.questionnaireresponse.QuestionnaireResponseStatus;
import com.rrsgroup.customer.dto.questionnaire.QuestionnaireDto;

import java.time.LocalDateTime;
import java.util.List;

public record QuestionnaireResponseDto(Long id, Long questionnaireId, QuestionnaireResponseStatus status,
                                       QuestionnaireDto questionnaire, CrmCustomer crmCustomer,
                                       List<QuestionnaireResponseAnswerDto> answers, Long predecessorId,
                                       LocalDateTime createdDate, LocalDateTime updatedDate, String createdBy, String updatedBy) {
}
