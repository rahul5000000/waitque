package com.rrsgroup.customer.dto.message;

import com.rrsgroup.common.dto.AddressDto;
import com.rrsgroup.common.dto.PhoneNumberDto;
import com.rrsgroup.customer.domain.message.MessageStatus;

import java.time.LocalDateTime;
import java.util.List;

public record MessageDto(Long id, MessageStatus status, String overrideFirstName,
                         String overrideLastName, AddressDto overrideAddress, PhoneNumberDto overridePhoneNumber,
                         String overrideEmail, String message, List<MessageStatusHistoryDto> statusHistory,
                         LocalDateTime createdDate, LocalDateTime updatedDate, String createdBy, String updatedBy) {
}
