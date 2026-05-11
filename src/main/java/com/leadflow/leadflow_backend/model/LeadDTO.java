package com.leadflow.leadflow_backend.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class LeadDTO {
    @LeadIdValid
    private String id;

    @Size(max = 255)
    private String userId;

    @NotNull
    @Size(max = 255)
    private String name;

    @NotNull
    @Size(max = 50)
    private String phone;

    @Size(max = 255)
    private String email;

    @Size(max = 100)
    private String source;

    @Size(max = 50)
    private String status;

    private String notes;

    private LocalDateTime lastContacted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}