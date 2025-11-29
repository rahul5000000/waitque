package com.rrsgroup.customer.dto;

import java.time.LocalDateTime;

public record UploadUrlDto(String url, String cdnBaseUrl, String rawPath, LocalDateTime validUntil) {
}
