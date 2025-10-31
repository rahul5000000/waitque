package com.rrsgroup.customer.dto.questionnaireresponse;

import com.rrsgroup.customer.domain.questionnaire.QuestionnaireQuestionDataType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuestionnaireResponseEmailAnswerDto extends QuestionnaireResponseAnswerDto {
    @NotBlank(message = "email answer is required")
    private String email;

    public QuestionnaireResponseEmailAnswerDto(Long id, Long questionnaireQuestionId, String email) {
        super(id, questionnaireQuestionId, QuestionnaireQuestionDataType.EMAIL);
        this.email = email;
    }
}
