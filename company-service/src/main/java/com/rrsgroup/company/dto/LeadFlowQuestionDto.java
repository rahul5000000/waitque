package com.rrsgroup.company.dto;

import com.rrsgroup.company.domain.LeadFlowQuestionDataType;

public record LeadFlowQuestionDto(Long id, String question, LeadFlowQuestionDataType dataType) {
}
