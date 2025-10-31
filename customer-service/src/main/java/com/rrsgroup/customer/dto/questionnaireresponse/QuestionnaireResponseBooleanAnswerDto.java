package com.rrsgroup.customer.dto.questionnaireresponse;

import com.rrsgroup.customer.domain.questionnaire.QuestionnaireQuestionDataType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuestionnaireResponseBooleanAnswerDto extends QuestionnaireResponseAnswerDto {
    @NotNull(message = "enabled answer is required")
    private Boolean enabled;

    public QuestionnaireResponseBooleanAnswerDto(Long id, Long questionnaireQuestionId, Boolean enabled) {
        super(id, questionnaireQuestionId, QuestionnaireQuestionDataType.BOOLEAN);
        this.enabled = enabled;
    }
}
