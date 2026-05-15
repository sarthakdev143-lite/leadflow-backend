package com.leadflow.leadflow_backend.exception;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ExceptionTest {

    @Test
    void testAppException() {
        AppException ex = new AppException("TEST_ERROR", "Test message", HttpStatus.BAD_REQUEST);
        assertEquals("TEST_ERROR", ex.getErrorCode());
        assertEquals("Test message", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void testResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User", "id", "123");
        assertEquals("RESOURCE_NOT_FOUND", ex.getErrorCode());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void testUnauthorizedException() {
        UnauthorizedException ex = new UnauthorizedException("Not authenticated");
        assertEquals("UNAUTHORIZED", ex.getErrorCode());
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    void testForbiddenException() {
        ForbiddenException ex = new ForbiddenException();
        assertEquals("FORBIDDEN", ex.getErrorCode());
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void testConflictException() {
        ConflictException ex = new ConflictException("Email already exists");
        assertEquals("CONFLICT", ex.getErrorCode());
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void testValidationException() {
        ValidationException ex = new ValidationException("Invalid input");
        assertEquals("VALIDATION_FAILED", ex.getErrorCode());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }
}