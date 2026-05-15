package com.leadflow.leadflow_backend.exception;

import org.springframework.http.HttpStatus;

public class AuthException extends AppException {
    public AuthException(String message) {
        super("AUTHENTICATION_FAILED", message, HttpStatus.UNAUTHORIZED);
    }

    public AuthException() {
        super("AUTHENTICATION_FAILED", "Invalid credentials", HttpStatus.UNAUTHORIZED);
    }
}