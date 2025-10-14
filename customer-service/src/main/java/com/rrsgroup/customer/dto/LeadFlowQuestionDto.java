package com.rrsgroup.customer.dto;

import com.rrsgroup.customer.domain.LeadFlowQuestionDataType;

public record LeadFlowQuestionDto(Long id, String question, LeadFlowQuestionDataType dataType, Boolean isRequired) {
}
