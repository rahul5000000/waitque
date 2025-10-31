package com.rrsgroup.customer.dto.questionnaireresponse;

import com.rrsgroup.customer.domain.questionnaire.QuestionnaireQuestionDataType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuestionnaireResponsePhoneAnswerDto extends QuestionnaireResponseAnswerDto {
    @NotBlank(message = "phoneNumber answer is required")
    private Long phoneNumber;

    public QuestionnaireResponsePhoneAnswerDto(Long id, Long questionnaireQuestionId, Long phoneNumber) {
        super(id, questionnaireQuestionId, QuestionnaireQuestionDataType.PHONE);
        this.phoneNumber = phoneNumber;
    }
}
