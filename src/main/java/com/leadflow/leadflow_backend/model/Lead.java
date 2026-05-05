package com.leadflow.leadflow_backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "leads")
public class Lead {

    @Id
    private String id;

    private String name;
    private String phone;
    private String source;
    private LeadStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
