package com.rrsgroup.customer.service;

import com.rrsgroup.customer.dto.UploadUrlDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.LocalDateTime;

@Service
public class UploadUrlDtoMapper {
    @Value("${CDN_BASE_URL}")
    private String cdnBaseUrl;

    public UploadUrlDto map(URL url, String rawPath, LocalDateTime validUntil) {
        return new UploadUrlDto(url.toString(), cdnBaseUrl + "/" + rawPath, validUntil);
    }
}
