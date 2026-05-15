package com.leadflow.leadflow_backend.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends AppException {
    public ForbiddenException(String message) {
        super("FORBIDDEN", message, HttpStatus.FORBIDDEN);
    }

    public ForbiddenException() {
        super("FORBIDDEN", "You don't have permission to access this resource", HttpStatus.FORBIDDEN);
    }
}