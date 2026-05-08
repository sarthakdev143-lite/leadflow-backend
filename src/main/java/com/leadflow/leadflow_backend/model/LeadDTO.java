package com.leadflow.leadflow_backend.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LeadDTO {

    @Size(max = 255)
    @LeadIdValid
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
