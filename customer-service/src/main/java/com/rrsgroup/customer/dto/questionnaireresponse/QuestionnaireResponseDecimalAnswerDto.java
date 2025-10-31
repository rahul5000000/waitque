package com.rrsgroup.customer.dto.questionnaireresponse;

import com.rrsgroup.customer.domain.questionnaire.QuestionnaireQuestionDataType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuestionnaireResponseDecimalAnswerDto extends QuestionnaireResponseAnswerDto {
    @NotNull(message = "decimal answer is required")
    private Double decimal;

    public QuestionnaireResponseDecimalAnswerDto(Long id, Long questionnaireQuestionId, Double decimal) {
        super(id, questionnaireQuestionId, QuestionnaireQuestionDataType.DECIMAL);
        this.decimal = decimal;
    }
}
