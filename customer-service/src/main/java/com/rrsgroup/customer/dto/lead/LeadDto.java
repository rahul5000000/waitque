package com.rrsgroup.customer.dto.lead;

import com.rrsgroup.common.dto.AddressDto;
import com.rrsgroup.common.dto.PhoneNumberDto;
import com.rrsgroup.customer.domain.CrmCustomer;
import com.rrsgroup.customer.domain.lead.LeadStatus;
import com.rrsgroup.customer.dto.LeadFlowDto;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;

public record LeadDto(Long id, Long leadFlowId, LeadStatus status, String overrideFirstName,
                      String overrideLastName, AddressDto overrideAddress, PhoneNumberDto overridePhoneNumber,
                      String overrideEmail, @Valid List<LeadAnswerDto> answers, CrmCustomer crmCustomer,
                      LeadFlowDto leadFlow, List<LeadStatusHistoryDto> statusHistory,
                      LocalDateTime createdDate, LocalDateTime updatedDate, String createdBy, String updatedBy) {
}