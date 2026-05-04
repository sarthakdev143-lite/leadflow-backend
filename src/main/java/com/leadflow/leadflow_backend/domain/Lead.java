package com.leadflow.leadflow_backend.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document
@Getter
@Setter
public class Lead {

    @NotNull
    @Id
    private String id;

    @NotNull
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

    private OffsetDateTime lastContacted;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

}
