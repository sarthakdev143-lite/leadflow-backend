package com.leadflow.leadflow_backend.rest;

import com.leadflow.leadflow_backend.exception.ResourceNotFoundException;
import com.leadflow.leadflow_backend.exception.UnauthorizedException;
import com.leadflow.leadflow_backend.model.LeadDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class DemoController {

    @GetMapping("/users/{id}")
    public ResponseEntity<String> getUser(@PathVariable String id) {
        if ("999".equals(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        return ResponseEntity.ok("User found: " + id);
    }

    @PostMapping("/register")
    public ResponseEntity<DemoUserResponse> registerUser(@Valid @RequestBody DemoUserRequest request) {
        log.info("Registering user: {}", request.getEmail());
        return ResponseEntity.ok(new DemoUserResponse(request.getEmail(), "Registered successfully"));
    }

    @GetMapping("/secure")
    public ResponseEntity<String> secureEndpoint(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid authorization header");
        }
        return ResponseEntity.ok("Access granted");
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<String> deleteItem(@PathVariable String id) {
        log.warn("Attempting to delete item: {}", id);
        return ResponseEntity.ok("Item deleted");
    }

    public static class DemoUserRequest {
        @NotBlank(message = "Name is required")
        private String name;

        @Email(message = "Email should be valid")
        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class DemoUserResponse {
        private String email;
        private String message;

        public DemoUserResponse(String email, String message) {
            this.email = email;
            this.message = message;
        }

        public String getEmail() { return email; }
        public String getMessage() { return message; }
    }
}