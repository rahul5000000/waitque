package com.rrsgroup.customer.dto;

import java.util.UUID;

public record CustomerDto(Long id, String crmCustomerId, UUID qrCode) {
}
