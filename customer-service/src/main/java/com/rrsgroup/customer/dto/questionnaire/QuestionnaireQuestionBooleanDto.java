package com.rrsgroup.customer.dto.questionnaire;

import com.rrsgroup.customer.domain.questionnaire.QuestionnaireQuestionDataType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuestionnaireQuestionBooleanDto extends QuestionnaireQuestionDto {
    private String falseText;
    private String trueText;

    public QuestionnaireQuestionBooleanDto(Long id, String question, QuestionnaireQuestionDataType dataType, Boolean isRequired, String questionGroup, String falseText, String trueText) {
        super(id, question, dataType, isRequired, questionGroup);
        this.falseText = falseText;
        this.trueText = trueText;
    }
}