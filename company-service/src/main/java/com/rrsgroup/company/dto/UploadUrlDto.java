package com.rrsgroup.company.dto;

import java.time.LocalDateTime;

public record UploadUrlDto(String url, LocalDateTime validUntil) {
}
