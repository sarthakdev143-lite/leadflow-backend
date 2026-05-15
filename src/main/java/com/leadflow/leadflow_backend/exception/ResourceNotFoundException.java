package com.leadflow.leadflow_backend.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends AppException {
    public ResourceNotFoundException(String resourceName, String field, Object value) {
        super(
            "RESOURCE_NOT_FOUND",
            String.format("%s not found with %s: %s", resourceName, field, value),
            HttpStatus.NOT_FOUND
        );
    }

    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }
}