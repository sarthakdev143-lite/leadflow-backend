package com.leadflow.leadflow_backend.service;
import com.leadflow.leadflow_backend.model.SendResponse;
import com.leadflow.leadflow_backend.domain.MessageLog;
import com.leadflow.leadflow_backend.domain.MessageStatus;
import com.leadflow.leadflow_backend.domain.MessageType;
import com.leadflow.leadflow_backend.teleException.TelegramException;
import com.leadflow.leadflow_backend.repos.MessageLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class TelegramService {

    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000L;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.chat.id}")
    private String chatId;

    @Autowired
    private MessageLogRepository messageLogRepository;

    @Autowired
    private RestTemplate restTemplate;

    public SendResponse sendMessage(String name, String phone, String source, String type, String customMessage, String leadChatId) {
        String messageText = getTemplate(type, name, phone, source, customMessage);
        log.info("Attempting to send Telegram message. Type: {}, Name: {}", type, name);

        // Fallback target initialization
        String targetChatId = this.chatId;

        // Verify if the incoming leadChatId is a valid positive or negative numeric string
        if (leadChatId != null && !leadChatId.trim().isEmpty() && leadChatId.trim().matches("-?\\d+")) {
            targetChatId = leadChatId.trim();
            log.info("Routing message directly to specific lead chatId: {}", targetChatId);
        } else {
            log.info("Provided leadChatId is text or invalid. Falling back to default configuration chatId: {}", this.chatId);
        }

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                log.info("Attempt {}/{}", attempt, MAX_RETRIES);

                String url = TELEGRAM_API_URL + botToken + "/sendMessage";

                Map<String, String> requestBody = new HashMap<>();
                requestBody.put("chat_id", targetChatId);
                requestBody.put("text", messageText);
                requestBody.put("parse_mode", "Markdown");

                ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);

                Map<String, Object> body = response.getBody();
                if (body == null || !Boolean.TRUE.equals(body.get("ok"))) {
                    throw new TelegramException("Telegram API returned unsuccessful response");
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> result = (Map<String, Object>) body.get("result");
                Integer messageId = (Integer) result.get("message_id");

                logMessage(targetChatId, messageText, type, MessageStatus.SUCCESS, messageId, null);
                log.info("Message sent successfully. Telegram messageId: {}", messageId);

                return new SendResponse(true, messageId, LocalDateTime.now());

            } catch (Exception e) {
                log.warn("Attempt {}/{} failed: {}", attempt, MAX_RETRIES, e.getMessage());

                if (attempt == MAX_RETRIES) {
                    logMessage(targetChatId, messageText, type, MessageStatus.FAILED, null, e.getMessage());
                    log.error("All {} attempts failed. Logging failure.", MAX_RETRIES);
                    throw new TelegramException("Failed to send Telegram message after " + MAX_RETRIES + " retries", e);
                }

                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new TelegramException("Retry interrupted", ie);
                }
            }
        }

        return null;
    }

    public String getTemplate(String type, String name, String phone, String source, String customMessage) {
        switch (type) {
            case "AUTO_NEW_LEAD":
                return " *New Lead Received!*\n\n"
                        + "*Name:* " + name + "\n"
                        + "*Phone:* " + phone + "\n"
                        + "*Source:* " + source + "\n\n"
                        + "Please follow up with this lead.";

            case "REMINDER":
                return " *Reminder*\n\n"
                        + "Lead *" + name + "* (" + phone + ") "
                        + "has been waiting for 24+ hours.\n"
                        + "Please take action.";

            case "FOLLOWUP":
                return " *Follow-up Needed*\n\n"
                        + "Lead *" + name + "* (" + phone + ") "
                        + "was contacted 2+ days ago.\n"
                        + "Time for a follow-up!";

            case "MANUAL":
                return (customMessage != null && !customMessage.isEmpty())
                        ? customMessage
                        : "No manual message content was provided.";

            default:
                return " *Lead Update*\n\n"
                        + "*Name:* " + name + "\n"
                        + "*Phone:* " + phone;
        }
    }

    private void logMessage(String chatId, String text, String type,
                            MessageStatus status, Integer msgId, String error) {
        try {
            MessageLog messageLog = new MessageLog();
            messageLog.setChatId(chatId);
            messageLog.setMessageText(text);
            messageLog.setMessageType(MessageType.valueOf(type));
            messageLog.setStatus(status);
            messageLog.setTelegramMessageId(msgId);
            messageLog.setErrorMessage(error);
            messageLog.setSentAt(LocalDateTime.now());

            log.info("Saving to MongoDB — status: {}, chatId: {}", status, chatId);
            MessageLog saved = messageLogRepository.save(messageLog);
            log.info("Saved successfully — id: {}", saved.getId());

        } catch (Exception e) {
            log.error("FAILED to save message log: {}", e.getMessage(), e);
        }
    }
}