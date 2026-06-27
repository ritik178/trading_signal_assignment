package com.zuvomo.signal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleBeanValidation(
            MethodArgumentNotValidException ex) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.toList());

        return buildError(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(ValidationException ex) {
        return buildError(HttpStatus.BAD_REQUEST, "Validation failed", ex.getErrors());
    }

    @ExceptionHandler(SignalNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(SignalNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), List.of());
    }

    private ResponseEntity<Map<String, Object>> buildError(
            HttpStatus status, String message, List<String> errors) {

        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status",    status.value(),
                "error",     message,
                "details",   errors
        );
        return ResponseEntity.status(status).body(body);
    }
}
