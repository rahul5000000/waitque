package com.rrsgroup.company.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.rrsgroup.company.domain.LeadFlowQuestionDataType;
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
        @JsonSubTypes.Type(value = LeadFlowBooleanQuestionDto.class, name = "BOOLEAN"),
        @JsonSubTypes.Type(value = LeadFlowQuestionAnswerDto.class, name = "TEXT"),
        @JsonSubTypes.Type(value = LeadFlowQuestionAnswerDto.class, name = "TEXTAREA"),
        @JsonSubTypes.Type(value = LeadFlowQuestionAnswerDto.class, name = "IMAGE"),
        @JsonSubTypes.Type(value = LeadFlowQuestionAnswerDto.class, name = "NUMBER"),
        @JsonSubTypes.Type(value = LeadFlowQuestionAnswerDto.class, name = "DECIMAL")
})
public abstract class LeadFlowQuestionDto {
    private Long id;
    @NotNull
    private String question;
    @NotNull
    private LeadFlowQuestionDataType dataType;
    @NotNull
    private Boolean isRequired;
}
