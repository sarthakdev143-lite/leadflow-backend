package com.leadflow.leadflow_backend.model;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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

    private String message;

    private String leadChatId;
}