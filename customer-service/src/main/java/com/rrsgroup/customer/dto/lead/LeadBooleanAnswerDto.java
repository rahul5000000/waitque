package com.rrsgroup.customer.dto.lead;

import com.rrsgroup.customer.domain.LeadFlowQuestionDataType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LeadBooleanAnswerDto extends LeadAnswerDto {
    @NotNull(message = "enabled answer is required")
    private Boolean enabled;

    public LeadBooleanAnswerDto(Long id, Long leadFlowQuestionId, Boolean enabled) {
        super(id, leadFlowQuestionId, LeadFlowQuestionDataType.BOOLEAN);
        this.enabled = enabled;
    }
}
