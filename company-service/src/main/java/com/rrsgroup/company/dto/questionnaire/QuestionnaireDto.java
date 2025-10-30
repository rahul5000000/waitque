package com.rrsgroup.company.dto.questionnaire;

import com.rrsgroup.company.domain.questionnaire.QuestionnaireStatus;

import java.util.List;

public record QuestionnaireDto(Long id, Long companyId, QuestionnaireStatus status, String name, String description, List<QuestionnairePageDto> pages, Long predecessorId) {
}
