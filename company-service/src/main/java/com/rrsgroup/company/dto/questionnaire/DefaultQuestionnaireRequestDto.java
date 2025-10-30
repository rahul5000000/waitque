package com.rrsgroup.company.dto.questionnaire;

import com.rrsgroup.company.domain.questionnaire.QuestionnaireType;

public record DefaultQuestionnaireRequestDto(Long companyId, QuestionnaireType type) {
}
