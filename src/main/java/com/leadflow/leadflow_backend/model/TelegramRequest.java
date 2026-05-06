package com.leadflow.leadflow_backend.model;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Telegram notification requests.
 * Aligned with LeadFlow CRM MVP requirements for lead tracking.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TelegramRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "Source is required")
    private String source;

    @NotBlank(message = "Type is required")
    private String type;
}