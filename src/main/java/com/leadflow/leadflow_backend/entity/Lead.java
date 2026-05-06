package com.leadflow.leadflow_backend.entity;

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
    private String status; // NEW, CONTACTED, FOLLOWED_UP

    private LocalDateTime createdAt;

    private LocalDateTime lastReminderSent;
    private LocalDateTime lastFollowupSent;

    }

