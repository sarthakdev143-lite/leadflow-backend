package com.leadflow.leadflow_backend.model;
import java.time.LocalDateTime;

public class SendResponse {

    private boolean success;
    private Integer messageId;
    private LocalDateTime timestamp;
    private String error;

    // Success response
    public SendResponse(boolean success, Integer messageId, LocalDateTime timestamp) {
        this.success = success;
        this.messageId = messageId;
        this.timestamp = timestamp;
    }

    // Error response
    public SendResponse(boolean success, String error) {
        this.success = success;
        this.error = error;
    }


    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public Integer getMessageId() { return messageId; }
    public void setMessageId(Integer messageId) { this.messageId = messageId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
