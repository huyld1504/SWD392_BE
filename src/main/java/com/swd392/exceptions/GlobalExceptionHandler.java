package com.swd392.exceptions;

import com.swd392.configs.RequestContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.swd392.dtos.common.ApiResponse;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleAllExceptions(Exception ex) {
        String requestId = RequestContext.getRequestId();
        log.error("[LAYER: UNKNOWN] Uncategorized Exception | RequestID: {} | Error: {}",
                requestId, ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.builder()
                        .success(false)
                        .message("Internal Server Error")
                        .requestId(requestId)
                        .timestamp(LocalDateTime.now().toString())
                        .build());
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(AppException ex) {
        log.error("[LAYER: {}] AppException | RequestID: {} | Status: {} | Message: {}",
                ex.getLayer(), ex.getRequestId(), ex.getStatusCode(), ex.getMessage());

        return ResponseEntity.status(ex.getStatusCode())
                .body(ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .requestId(ex.getRequestId())
                        .timestamp(ex.getTimestamp().toString())
                        .build());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        String requestId = RequestContext.getRequestId();
        log.error("[LAYER: SERVICE] Resource Not Found | RequestID: {} | Message: {}",
                requestId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .requestId(requestId)
                        .timestamp(LocalDateTime.now().toString())
                        .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException ex) {
        String requestId = RequestContext.getRequestId();
        log.error("[LAYER: SECURITY] Access Denied | RequestID: {} | Error: {}",
                requestId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.builder()
                        .success(false)
                        .message("You do not have permission")
                        .requestId(requestId)
                        .timestamp(LocalDateTime.now().toString())
                        .build());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException ex) {
        String requestId = RequestContext.getRequestId();
        log.error("[LAYER: SECURITY] Authentication Failed | RequestID: {} | Error: {}",
                requestId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.builder()
                        .success(false)
                        .message("Unauthenticated")
                        .requestId(requestId)
                        .timestamp(LocalDateTime.now().toString())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String requestId = RequestContext.getRequestId();
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        log.error("[LAYER: CONTROLLER] Validation Failed | RequestID: {} | Errors: {}",
                requestId, errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.builder()
                        .success(false)
                        .message("Validation Failed")
                        .data(errors)
                        .requestId(requestId)
                        .timestamp(LocalDateTime.now().toString())
                        .build());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex) {
        String requestId = RequestContext.getRequestId();
        log.error("[LAYER: CONTROLLER] File Size Exceeded | RequestID: {} | Error: {}",
                requestId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiResponse.builder()
                        .success(false)
                        .message(
                                "File size exceeds the allowed limit. Maximum file size: 5MB, Maximum request size: 25MB")
                        .requestId(requestId)
                        .timestamp(LocalDateTime.now().toString())
                        .build());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        String requestId = RequestContext.getRequestId();
        log.error("[LAYER: CONTROLLER] Resource Not Found | RequestID: {} | URI: {}",
                requestId, ex.getRequestURL());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.builder()
                        .success(false)
                        .message("Resource Not Found")
                        .requestId(requestId)
                        .timestamp(LocalDateTime.now().toString())
                        .build());
    }
}
