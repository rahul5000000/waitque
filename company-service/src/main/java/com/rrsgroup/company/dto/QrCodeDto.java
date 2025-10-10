package com.rrsgroup.company.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record QrCodeDto(Long id, Long companyId, Long customerId, UUID qrCode, LocalDateTime createdDate, LocalDateTime updatedDate, String createdBy, String updatedBy) {
}
