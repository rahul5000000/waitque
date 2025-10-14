package com.rrsgroup.customer.dto.lead;

import com.rrsgroup.customer.domain.LeadFlowQuestionDataType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LeadNumberAnswerDto extends LeadAnswerDto {
    @NotNull(message = "number answer is required")
    private Long number;

    public LeadNumberAnswerDto(Long id, Long leadFlowQuestionId, Long number) {
        super(id, leadFlowQuestionId, LeadFlowQuestionDataType.NUMBER);
        this.number = number;
    }
}