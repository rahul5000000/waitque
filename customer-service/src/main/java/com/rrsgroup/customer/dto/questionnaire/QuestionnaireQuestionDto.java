package com.rrsgroup.customer.dto.questionnaire;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.rrsgroup.customer.domain.questionnaire.QuestionnaireQuestionDataType;
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
        visible = true,
        defaultImpl = QuestionnaireQuestionAnswerDto.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = QuestionnaireQuestionBooleanDto.class, name = "BOOLEAN")
})
public abstract class QuestionnaireQuestionDto {
    private Long id;
    private String question;
    private QuestionnaireQuestionDataType dataType;
    private Boolean isRequired;
    private String questionGroup;
}