package com.leadflow.leadflow_backend.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UserDTO {

    @Size(max = 255)
    @UserIdValid
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
