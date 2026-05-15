package com.leadflow.leadflow_backend.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends AppException {
    public ConflictException(String message) {
        super("CONFLICT", message, HttpStatus.CONFLICT);
    }

    public ConflictException(String resource, String field, Object value) {
        super(
            "CONFLICT",
            String.format("%s already exists with %s: %s", resource, field, value),
            HttpStatus.CONFLICT
        );
    }
}