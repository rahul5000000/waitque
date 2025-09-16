package com.rrsgroup.company.dto;

import com.rrsgroup.company.domain.Status;

import java.util.List;

public record LeadFlowDto(Long id, Long companyId, Status status, String name, String iconUrl, String buttonText,
                          String title, String confirmationMessageHeader, String confirmationMessage1,
                          String confirmationMessage2, String confirmationMessage3, Integer ordinal,
                          List<LeadFlowQuestionDto> questions, Long predecessorId) {
}
