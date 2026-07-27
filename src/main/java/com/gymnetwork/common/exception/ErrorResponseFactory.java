package com.gymnetwork.common.exception;

import com.gymnetwork.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ErrorResponseFactory {

    private ErrorResponseFactory() {
    }

    public static ApiResponse<Void> build(HttpStatus status, String message, HttpServletRequest request) {
        return build(status, message, null, request);
    }

    public static ApiResponse<Void> build(HttpStatus status, String message, Object details, HttpServletRequest request) {
        return ApiResponse.error(message, errorBody(status, details, request));
    }

    public static Map<String, Object> errorBody(HttpStatus status, Object details, HttpServletRequest request) {
        Map<String, Object> errors = new LinkedHashMap<>();
        errors.put("status", status.value());
        errors.put("code", status.name());
        errors.put("path", request.getRequestURI());
        errors.put("timestamp", Instant.now());
        if (details != null) {
            errors.put("details", details);
        }
        return errors;
    }
}
