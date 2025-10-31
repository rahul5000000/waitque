package com.rrsgroup.customer.dto.questionnaire;

import com.rrsgroup.customer.domain.questionnaire.QuestionnaireStatus;

import java.util.List;

public record QuestionnaireDto(Long id, Long companyId, QuestionnaireStatus status, String name, String description, List<QuestionnairePageDto> pages, Long predecessorId) {
}
