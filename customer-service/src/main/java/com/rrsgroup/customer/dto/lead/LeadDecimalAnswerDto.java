package com.rrsgroup.customer.dto.lead;

import com.rrsgroup.customer.domain.LeadFlowQuestionDataType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LeadDecimalAnswerDto extends LeadAnswerDto {
    @NotNull(message = "decimal answer is required")
    private Double decimal;

    public LeadDecimalAnswerDto(Long id, Long leadFlowQuestionId, Double decimal) {
        super(id, leadFlowQuestionId, LeadFlowQuestionDataType.DECIMAL);
        this.decimal = decimal;
    }
}
