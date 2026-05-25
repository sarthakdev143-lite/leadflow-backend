package com.leadflow.leadflow_backend.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user_notes")
@Getter
@Setter
public class Note {

    @Id
    private String id;

    @Size(max = 255)
    private String userId;

    @NotBlank(message = "Note content cannot be blank")
    private String content;

    @Size(max = 255)
    private String createdBy;

    private LocalDateTime createdAt;
}