package com.leadflow.leadflow_backend.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleAppException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User", "id", "999");
        ResponseEntity<ErrorResponse> response = handler.handleAppException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("RESOURCE_NOT_FOUND", body.getErrorCode());
        assertEquals(404, body.getStatus());
        assertNotNull(body.getTimestamp());
    }

    @Test
    void testHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid parameter");
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgumentException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("VALIDATION_FAILED", body.getErrorCode());
        assertEquals("Invalid parameter", body.getErrorMessage());
    }

    @Test
    void testHandleGenericException() {
        Exception ex = new RuntimeException("Unexpected error");
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("INTERNAL_SERVER_ERROR", body.getErrorCode());
        assertEquals("Something went wrong. Please try again later.", body.getErrorMessage());
        assertNull(body.getErrors());
    }

    @Test
    void testErrorResponseTimestampIsRecent() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        ResourceNotFoundException ex = new ResourceNotFoundException("Test", "id", "1");
        ResponseEntity<ErrorResponse> response = handler.handleAppException(ex);
        
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        ErrorResponse body = response.getBody();
        
        assertNotNull(body.getTimestamp());
        assertTrue(body.getTimestamp().isAfter(before));
        assertTrue(body.getTimestamp().isBefore(after));
    }
}