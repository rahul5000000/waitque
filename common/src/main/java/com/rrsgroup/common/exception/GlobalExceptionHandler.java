package com.rrsgroup.common.exception;

import com.rrsgroup.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@ControllerAdvice
@Log4j2
public class GlobalExceptionHandler {
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleValidationConstraintViolations(ConstraintViolationException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                getConstraintViolationExceptionMessage(ex),
                request.getMethod() + " " + request.getRequestURI());
        return ResponseEntity.status(status).body(response);
    }

    private String getConstraintViolationExceptionMessage(ConstraintViolationException ex) {
        try {
            return ex.getConstraintViolations().stream()
                    .map(violation -> "'" + violation.getPropertyPath() + "' " + violation.getMessage()).collect(Collectors.joining(", "));
        } catch (Exception e) {
            log.error("Failed to parse ConstraintViolationException", e);
            return "Unknown error";
        }
    }

    @ExceptionHandler(IllegalUpdateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalUpdateException(IllegalUpdateException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getMethod() + " " + request.getRequestURI());
        return ResponseEntity.status(status).body(response);
    }
}
