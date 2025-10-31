package com.rrsgroup.customer.dto.questionnaireresponse;

import com.rrsgroup.customer.domain.questionnaire.QuestionnaireQuestionDataType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuestionnaireResponseTextAnswerDto extends QuestionnaireResponseAnswerDto {
    @NotBlank(message = "text answer is required")
    private String text;

    public QuestionnaireResponseTextAnswerDto(Long id, Long questionnaireQuestionId, String text) {
        super(id, questionnaireQuestionId, QuestionnaireQuestionDataType.TEXT);
        this.text = text;
    }
}
