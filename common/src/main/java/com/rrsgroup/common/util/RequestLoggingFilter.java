package com.rrsgroup.common.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Log4j2
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID = "correlationId";
    private static final int MAX_BODY_SIZE = 10 * 1024; // 10 KB
    private static final String ACTUATOR_HEALTH_PATH = "/actuator/health";

    // Fields to mask
    private static final List<String> SENSITIVE_FIELDS = Arrays.asList(
            "password",
            "token",
            "access_token",
            "refresh_token",
            "authorization",
            "secret",
            "ssn"
    );

    // Regex for masking sensitive JSON fields
    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
            "(?i)\"(" + String.join("|", SENSITIVE_FIELDS) + ")\"\\s*:\\s*\"(.*?)\""
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper res = new ContentCachingResponseWrapper(response);

        String correlationId = request.getHeader("X-Correlation-Id");
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(CORRELATION_ID, correlationId);
        res.setHeader("X-Correlation-Id", correlationId);

        long start = System.currentTimeMillis();

        try {
            chain.doFilter(req, res);
        } finally {
            long duration = System.currentTimeMillis() - start;

            if (!shouldSkipLogging(req, res)) {
                logRequest(req, correlationId);
                logResponse(res, correlationId, duration);
            }

            MDC.remove(CORRELATION_ID);
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, String correlationId) {

        String rawBody = getLimitedBody(request.getContentAsByteArray());
        String masked = maskSensitiveFields(rawBody);

        log.info("> REQUEST[{}] | Method: {} | URI: {} | Query: {} | Body: {}",
                correlationId,
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                masked
        );
    }

    private void logResponse(ContentCachingResponseWrapper response,
                             String correlationId,
                             long duration) throws IOException {

        String rawBody = getLimitedBody(response.getContentAsByteArray());
        String masked = maskSensitiveFields(rawBody);

        log.info("< RESPONSE[{}] | Status: {} | Duration: {} ms | Body: {}",
                correlationId,
                response.getStatus(),
                duration,
                masked
        );

        response.copyBodyToResponse();
    }

    // 🔒 MASK sensitive fields in JSON bodies
    private String maskSensitiveFields(String body) {
        if (body == null || body.isEmpty()) {
            return body;
        }

        return SENSITIVE_PATTERN.matcher(body)
                .replaceAll("\"$1\":\"***MASKED***\"");
    }

    // ✂️ Limit request/response bodies to 10 KB
    private String getLimitedBody(byte[] content) {
        if (content == null || content.length == 0) return "";

        int length = Math.min(content.length, MAX_BODY_SIZE);

        String body = new String(content, 0, length, StandardCharsets.UTF_8);

        if (content.length > MAX_BODY_SIZE) {
            body += "\n\n--- TRUNCATED (over 10 KB) ---";
        }

        return body;
    }

    private boolean shouldSkipLogging(HttpServletRequest request,
                                      HttpServletResponse response) {
        return ACTUATOR_HEALTH_PATH.equals(request.getRequestURI())
                && response.getStatus() == HttpServletResponse.SC_OK;
    }
}
