package com.rrsgroup.customer.dto.lead;

import com.rrsgroup.customer.domain.LeadFlowQuestionDataType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LeadTextAreaAnswerDto extends LeadAnswerDto {
    @NotBlank(message = "paragraph answer is required")
    private String paragraph;

    public LeadTextAreaAnswerDto(Long id, Long leadFlowQuestionId, String paragraph) {
        super(id, leadFlowQuestionId, LeadFlowQuestionDataType.TEXTAREA);
        this.paragraph = paragraph;
    }
}
