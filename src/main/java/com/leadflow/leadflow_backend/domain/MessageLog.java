package com.leadflow.leadflow_backend.domain;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "message_logs")
public class MessageLog {
    @Id
    private String id;
    private String chatId;
    private String messageText;
    private MessageType messageType;
    private MessageStatus status;
    private Integer telegramMessageId;
    private String errorMessage;
    private LocalDateTime sentAt;
    private String recipient;
    private String channel;
}
