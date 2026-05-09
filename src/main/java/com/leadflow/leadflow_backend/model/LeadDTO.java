package com.leadflow.leadflow_backend.model;

import com.leadflow.leadflow_backend.domain.LeadStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LeadDTO {

    private String id;
    private String name;
    private String phone;
    private String source;
    private String notes;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}