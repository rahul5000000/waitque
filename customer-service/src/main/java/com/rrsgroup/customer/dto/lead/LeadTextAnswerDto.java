package com.rrsgroup.customer.dto.lead;

import com.rrsgroup.customer.domain.LeadFlowQuestionDataType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LeadTextAnswerDto extends LeadAnswerDto {
    @NotBlank(message = "text answer is required")
    private String text;

    public LeadTextAnswerDto(Long id, Long leadFlowQuestionId, String text) {
        super(id, leadFlowQuestionId, LeadFlowQuestionDataType.TEXT);
        this.text = text;
    }
}