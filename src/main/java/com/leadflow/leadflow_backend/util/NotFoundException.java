package com.leadflow.leadflow_backend.util;

import com.leadflow.leadflow_backend.exception.AppException;
import org.springframework.http.HttpStatus;

public class NotFoundException extends AppException {
    public NotFoundException() {
        super("NOT_FOUND", "Resource not found", HttpStatus.NOT_FOUND);
    }

    public NotFoundException(final String message) {
        super("NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }
}