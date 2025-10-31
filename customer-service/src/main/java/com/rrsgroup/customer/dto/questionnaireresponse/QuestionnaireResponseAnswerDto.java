package com.rrsgroup.customer.dto.questionnaireresponse;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.rrsgroup.customer.domain.questionnaire.QuestionnaireQuestionDataType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "dataType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = QuestionnaireResponseBooleanAnswerDto.class, name = "BOOLEAN"),
        @JsonSubTypes.Type(value = QuestionnaireResponseTextAnswerDto.class, name = "TEXT"),
        @JsonSubTypes.Type(value = QuestionnaireResponseTextAreaAnswerDto.class, name = "TEXTAREA"),
        @JsonSubTypes.Type(value = QuestionnaireResponseImageAnswerDto.class, name = "IMAGE"),
        @JsonSubTypes.Type(value = QuestionnaireResponseNumberAnswerDto.class, name = "NUMBER"),
        @JsonSubTypes.Type(value = QuestionnaireResponseDecimalAnswerDto.class, name = "DECIMAL"),
        @JsonSubTypes.Type(value = QuestionnaireResponsePhoneAnswerDto.class, name = "PHONE"),
        @JsonSubTypes.Type(value = QuestionnaireResponseEmailAnswerDto.class, name = "EMAIL"),
})
public abstract class QuestionnaireResponseAnswerDto {
    private Long id;
    @NotNull(message = "questionnaireQuestionId is required")
    private Long questionnaireQuestionId;
    @NotNull(message = "dataType is required")
    private QuestionnaireQuestionDataType dataType;
}
