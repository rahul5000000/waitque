package com.rrsgroup.customer.dto;

import com.rrsgroup.customer.domain.MobileLogLevel;

import java.util.Map;

public record MobileLogDto(MobileLogLevel level, String platform, String page, String message, Map<String, Object> json) {
}
