package com.leadflow.leadflow_backend.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends AppException {
    public ValidationException(String message) {
        super("VALIDATION_FAILED", message, HttpStatus.BAD_REQUEST);
    }
}