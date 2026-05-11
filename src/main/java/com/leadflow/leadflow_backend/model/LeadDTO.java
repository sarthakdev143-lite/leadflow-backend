package com.leadflow.leadflow_backend.model;
import com.leadflow.leadflow_backend.domain.LeadStatus;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;


@Data
public class LeadDTO {
    @LeadIdValid
    private String id;

    @Size(max = 255)
    private String userId;

    @NotNull
    @Size(max = 255)
    private String name;
    private String phone;
    private String source;
    private String notes;

    private LocalDateTime lastContacted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}