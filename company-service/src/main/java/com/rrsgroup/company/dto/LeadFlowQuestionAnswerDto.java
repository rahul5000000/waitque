package com.rrsgroup.company.dto;

import com.rrsgroup.company.domain.LeadFlowQuestionDataType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LeadFlowQuestionAnswerDto extends LeadFlowQuestionDto {
    public LeadFlowQuestionAnswerDto(Long id, String question, LeadFlowQuestionDataType dataType, Boolean isRequired) {
        super(id, question, dataType, isRequired);
    }
}
