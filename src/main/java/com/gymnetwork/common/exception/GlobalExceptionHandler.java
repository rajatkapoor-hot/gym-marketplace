package com.gymnetwork.common.exception;

import com.gymnetwork.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.error("Resource not found exception: {}", ex.getMessage());
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        log.error("Bad request exception: {}", ex.getMessage());
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        log.error("Unauthorized exception: {}", ex.getMessage());
        return error(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(ForbiddenException ex, HttpServletRequest request) {
        log.error("Forbidden exception: {}", ex.getMessage());
        return error(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(ConflictException ex, HttpServletRequest request) {
        log.error("Conflict exception: {}", ex.getMessage());
        return error(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        log.error("Bad credentials: {}", ex.getMessage());
        return error(HttpStatus.UNAUTHORIZED, "Invalid email or password", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.error("Access denied: {}", ex.getMessage());
        return error(HttpStatus.FORBIDDEN, "Access denied: Insufficient permissions", request);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLocking(ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {
        log.error("Optimistic locking failure: {}", ex.getMessage());
        return error(HttpStatus.CONFLICT, "Concurrent update detected. Please try your operation again.", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.error("Validation failed: {}", errors);
        return error(HttpStatus.BAD_REQUEST, "Validation failed", errors, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled Exception occurred: ", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.", request);
    }

    private ResponseEntity<ApiResponse<Void>> error(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(ErrorResponseFactory.build(status, message, request));
    }

    private ResponseEntity<ApiResponse<Void>> error(HttpStatus status, String message, Object details, HttpServletRequest request) {
        return ResponseEntity.status(status).body(ErrorResponseFactory.build(status, message, details, request));
    }
}
