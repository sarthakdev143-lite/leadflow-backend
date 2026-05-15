package com.leadflow.leadflow_backend.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex) {
        log.warn("Business exception: {} - {}", ex.getErrorCode(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
            .errorCode(ex.getErrorCode())
            .errorMessage(ex.getMessage())
            .status(ex.getStatus().value())
            .timestamp(LocalDateTime.now())
            .build();

        return new ResponseEntity<>(response, ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse response = ErrorResponse.builder()
            .errorCode("VALIDATION_FAILED")
            .errorMessage("Validation failed")
            .status(HttpStatus.BAD_REQUEST.value())
            .timestamp(LocalDateTime.now())
            .errors(ErrorResponse.FieldErrors.builder().fieldErrors(errors).build())
            .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Type mismatch: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
            .errorCode("VALIDATION_FAILED")
            .errorMessage(String.format("Invalid value for parameter '%s'", ex.getName()))
            .status(HttpStatus.BAD_REQUEST.value())
            .timestamp(LocalDateTime.now())
            .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Data integrity violation", ex);

        ErrorResponse response = ErrorResponse.builder()
            .errorCode("CONFLICT")
            .errorMessage("Data integrity violation occurred")
            .status(HttpStatus.CONFLICT.value())
            .timestamp(LocalDateTime.now())
            .build();

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException ex) {
        log.error("Database error", ex);

        ErrorResponse response = ErrorResponse.builder()
            .errorCode("DATABASE_ERROR")
            .errorMessage("Database operation failed")
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .timestamp(LocalDateTime.now())
            .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
            .errorCode("VALIDATION_FAILED")
            .errorMessage(ex.getMessage())
            .status(HttpStatus.BAD_REQUEST.value())
            .timestamp(LocalDateTime.now())
            .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);

        ErrorResponse response = ErrorResponse.builder()
            .errorCode("INTERNAL_SERVER_ERROR")
            .errorMessage("Something went wrong. Please try again later.")
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .timestamp(LocalDateTime.now())
            .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}