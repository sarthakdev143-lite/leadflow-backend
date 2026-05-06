package com.leadflow.leadflow_backend.teleException;

public class TelegramException extends RuntimeException {

    public TelegramException(String message) {
        super(message);
    }

    public TelegramException(String message, Throwable cause) {
        super(message, cause);
    }
}



