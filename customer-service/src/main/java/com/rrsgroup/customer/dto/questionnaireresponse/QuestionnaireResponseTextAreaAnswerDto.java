package com.rrsgroup.customer.dto.questionnaireresponse;

import com.rrsgroup.customer.domain.questionnaire.QuestionnaireQuestionDataType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuestionnaireResponseTextAreaAnswerDto extends QuestionnaireResponseAnswerDto {
    @NotBlank(message = "paragraph answer is required")
    private String paragraph;

    public QuestionnaireResponseTextAreaAnswerDto(Long id, Long questionnaireQuestionId, String paragraph) {
        super(id, questionnaireQuestionId, QuestionnaireQuestionDataType.TEXTAREA);
        this.paragraph = paragraph;
    }
}
