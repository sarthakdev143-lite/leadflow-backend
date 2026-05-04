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
public class User {

    @NotNull
    @Id
    private String id;

    @NotNull
    @Size(max = 255)
    private String email;

    @NotNull
    @Size(max = 255)
    private String passwordHash;

    @NotNull
    @Size(max = 255)
    private String fullName;

    @Size(max = 50)
    private String phone;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

}
