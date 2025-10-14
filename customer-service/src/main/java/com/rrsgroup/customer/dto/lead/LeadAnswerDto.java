package com.rrsgroup.customer.dto.lead;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.rrsgroup.customer.domain.LeadFlowQuestionDataType;
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
        @JsonSubTypes.Type(value = LeadBooleanAnswerDto.class, name = "BOOLEAN"),
        @JsonSubTypes.Type(value = LeadTextAnswerDto.class, name = "TEXT"),
        @JsonSubTypes.Type(value = LeadTextAreaAnswerDto.class, name = "TEXTAREA"),
        @JsonSubTypes.Type(value = LeadImageAnswerDto.class, name = "IMAGE"),
        @JsonSubTypes.Type(value = LeadNumberAnswerDto.class, name = "NUMBER"),
        @JsonSubTypes.Type(value = LeadDecimalAnswerDto.class, name = "DECIMAL")
})
public abstract class LeadAnswerDto {
    private Long id;
    @NotNull(message = "leadFlowQuestionId is required")
    private Long leadFlowQuestionId;
    @NotNull(message = "dataType is required")
    private LeadFlowQuestionDataType dataType;
}
