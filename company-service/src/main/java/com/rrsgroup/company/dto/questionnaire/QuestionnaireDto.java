package com.rrsgroup.company.dto.questionnaire;

import java.util.List;

public record QuestionnaireDto(Long id, Long companyId, String name, String description, List<QuestionnairePageDto> pages, Long predecessorId) {
}
