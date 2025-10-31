package com.rrsgroup.customer.dto.questionnaireresponse;

import com.rrsgroup.customer.domain.questionnaire.QuestionnaireQuestionDataType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuestionnaireResponseNumberAnswerDto extends QuestionnaireResponseAnswerDto {
    @NotNull(message = "number answer is required")
    private Long number;

    public QuestionnaireResponseNumberAnswerDto(Long id, Long questionnaireQuestionId, Long number) {
        super(id, questionnaireQuestionId, QuestionnaireQuestionDataType.NUMBER);
        this.number = number;
    }
}
