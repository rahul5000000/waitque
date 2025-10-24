package com.rrsgroup.customer.dto;

import com.rrsgroup.customer.domain.LeadFlowQuestionDataType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LeadFlowQuestionAnswerDto extends LeadFlowQuestionDto {
    public LeadFlowQuestionAnswerDto(Long id, String question, LeadFlowQuestionDataType dataType, Boolean isRequired) {
        super(id, question, dataType, isRequired);
    }
}