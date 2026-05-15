package com.leadflow.leadflow_backend.exception;

import org.springframework.http.HttpStatus;

public class ExternalServiceException extends AppException {
    public ExternalServiceException(String serviceName, String message) {
        super(
            "EXTERNAL_SERVICE_ERROR",
            String.format("Error communicating with %s: %s", serviceName, message),
            HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    public ExternalServiceException(String serviceName) {
        super(
            "EXTERNAL_SERVICE_ERROR",
            String.format("%s service is currently unavailable", serviceName),
            HttpStatus.SERVICE_UNAVAILABLE
        );
    }
}