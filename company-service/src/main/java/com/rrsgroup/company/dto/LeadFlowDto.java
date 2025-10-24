package com.rrsgroup.company.dto;

import com.rrsgroup.company.domain.LeadFlowStatus;

import java.util.List;

public record LeadFlowDto(Long id, Long companyId, LeadFlowStatus status, String name, String icon, String buttonText,
                          String title, String confirmationMessageHeader, String confirmationMessage1,
                          String confirmationMessage2, String confirmationMessage3, Integer ordinal,
                          List<LeadFlowQuestionDto> questions, Long predecessorId) {
}
