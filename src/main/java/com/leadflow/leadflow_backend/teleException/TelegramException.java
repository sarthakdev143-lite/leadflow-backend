package com.leadflow.leadflow_backend.teleException;

import com.leadflow.leadflow_backend.exception.AppException;
import org.springframework.http.HttpStatus;

public class TelegramException extends AppException {
    public TelegramException(String message) {
        super("TELEGRAM_ERROR", message, HttpStatus.SERVICE_UNAVAILABLE);
    }

    public TelegramException(String message, Throwable cause) {
        super("TELEGRAM_ERROR", message, HttpStatus.SERVICE_UNAVAILABLE);
        initCause(cause);
    }
}