package com.rrsgroup.customer.dto.lead;

import com.rrsgroup.customer.domain.LeadFlowQuestionDataType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LeadImageAnswerDto extends LeadAnswerDto {
    @NotBlank(message = "url is required")
    private String url;

    public LeadImageAnswerDto(Long id, Long leadFlowQuestionId, String url) {
        super(id, leadFlowQuestionId, LeadFlowQuestionDataType.IMAGE);
        this.url = url;
    }
}
