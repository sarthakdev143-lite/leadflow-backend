package com.leadflow.leadflow_backend.teleException;

import com.leadflow.leadflow_backend.exception.AppException;
import org.springframework.http.HttpStatus;

public class EmailException extends AppException {
    public EmailException(String message) {
        super("EMAIL_ERROR", message, HttpStatus.SERVICE_UNAVAILABLE);
    }

    public EmailException(String message, Throwable cause) {
        super("EMAIL_ERROR", message, HttpStatus.SERVICE_UNAVAILABLE);
        initCause(cause);
    }
}