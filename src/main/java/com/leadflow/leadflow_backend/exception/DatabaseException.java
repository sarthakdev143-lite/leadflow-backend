package com.leadflow.leadflow_backend.exception;

import org.springframework.http.HttpStatus;

public class DatabaseException extends AppException {
    public DatabaseException(String message) {
        super("DATABASE_ERROR", message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public DatabaseException(String message, Throwable cause) {
        super("DATABASE_ERROR", message, HttpStatus.INTERNAL_SERVER_ERROR);
        initCause(cause);
    }
}