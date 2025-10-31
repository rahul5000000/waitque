package com.rrsgroup.customer.dto.questionnaireresponse;

import com.rrsgroup.customer.domain.questionnaire.QuestionnaireQuestionDataType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuestionnaireResponseImageAnswerDto extends QuestionnaireResponseAnswerDto {
    @NotBlank(message = "url is required")
    private String url;

    public QuestionnaireResponseImageAnswerDto(Long id, Long questionnaireQuestionId, String url) {
        super(id, questionnaireQuestionId, QuestionnaireQuestionDataType.IMAGE);
        this.url = url;
    }
}
