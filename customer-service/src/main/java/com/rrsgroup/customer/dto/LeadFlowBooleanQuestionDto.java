package com.rrsgroup.customer.dto;

import com.rrsgroup.customer.domain.LeadFlowQuestionDataType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LeadFlowBooleanQuestionDto extends LeadFlowQuestionDto {
    @NotNull(message = "false, i.e. toggle off, text label is required")
    private String falseText;

    @NotNull(message = "true, i.e. toggle on, text label is required")
    private String trueText;

    public LeadFlowBooleanQuestionDto(Long id, String question, LeadFlowQuestionDataType dataType, Boolean isRequired, String falseText, String trueText) {
        super(id, question, dataType, isRequired);
        this.falseText = falseText;
        this.trueText = trueText;
    }
}

