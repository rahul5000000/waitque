package com.rrsgroup.company.dto.questionnaire;

import com.rrsgroup.company.domain.questionnaire.QuestionnaireQuestionDataType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuestionnaireQuestionAnswerDto extends QuestionnaireQuestionDto {
    public QuestionnaireQuestionAnswerDto(Long id, String question, QuestionnaireQuestionDataType dataType, Boolean isRequired, String questionGroup) {
        super(id, question, dataType, isRequired, questionGroup);
    }
}
