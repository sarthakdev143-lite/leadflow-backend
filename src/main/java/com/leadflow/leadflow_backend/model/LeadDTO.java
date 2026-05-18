package com.leadflow.leadflow_backend.model;
import com.leadflow.leadflow_backend.domain.LeadStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import lombok.*;

@Data
@Getter
@Setter
public class LeadDTO {

    private String id;

    @Size(max = 255)
    private String userId;

    @NotNull
    @Size(max = 255)
    private String name;
    private String phone;
    private String source;
    private String notes;
    private String  email;
    private String status;

    private String createdBy;
    @Size(max = 255)
    private String email;
    private String status;
    private LocalDateTime lastContacted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}