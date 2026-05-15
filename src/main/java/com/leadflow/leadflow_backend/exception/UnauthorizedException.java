package com.leadflow.leadflow_backend.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends AppException {
    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message, HttpStatus.UNAUTHORIZED);
    }

    public UnauthorizedException() {
        super("UNAUTHORIZED", "Authentication required", HttpStatus.UNAUTHORIZED);
    }
}