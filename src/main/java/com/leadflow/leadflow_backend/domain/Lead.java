package com.leadflow.leadflow_backend.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "leads")
public class Lead {

    @Id
    private String id;
    @NotBlank(message = "Name is mandatory")
    @Size(min = 2, message = "Name should have at least 2 characters")
    private String name;

    @Size(max = 255)
    private String email;

    @NotBlank(message = "Phone number is mandatory")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid phone number: Must be 10 digits and start with 6-9")
    private String phone;
    private String source;
    private LeadStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}