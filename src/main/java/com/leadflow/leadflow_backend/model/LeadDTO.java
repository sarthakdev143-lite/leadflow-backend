package com.leadflow.leadflow_backend.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class LeadDTO {

    private String id;

    @Size(max = 255)
    private String userId;

    @NotBlank(message = "Name is required")
    @Size(max = 255)
    private String name;

    @Email(message = "Email should be valid")
    @Size(max = 255)
    private String email;

    @Size(max = 20)
    private String phone;

    @Size(max = 100)
    private String source;

    private String notes;
    private String status;

    private LocalDateTime lastContacted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}