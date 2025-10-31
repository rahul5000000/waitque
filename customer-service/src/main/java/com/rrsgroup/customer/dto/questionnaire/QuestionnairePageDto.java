package com.rrsgroup.customer.dto.questionnaire;

import java.util.List;

public record QuestionnairePageDto(Long id, String pageTitle, Integer pageNumber, List<QuestionnaireQuestionDto> questions) {
}
