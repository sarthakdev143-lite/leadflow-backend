package com.leadflow.leadflow_backend.service;

import com.leadflow.leadflow_backend.domain.MessageLog;
import com.leadflow.leadflow_backend.domain.MessageStatus;
import com.leadflow.leadflow_backend.domain.MessageType;
import com.leadflow.leadflow_backend.teleException.EmailException;
import com.leadflow.leadflow_backend.model.SendResponse;
import com.leadflow.leadflow_backend.repos.MessageLogRepository;
import com.leadflow.leadflow_backend.util.EmailTemplates;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000L;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    private MessageLogRepository messageLogRepository;

    // ─── Public: Send Email ───────────────────────────────────────────────────────

    public SendResponse sendEmail(String toEmail, String leadName, String type) {
        logger.info("Attempting to send email. Type: {}, To: {}", type, toEmail);

        String subject = getSubject(type);
        String body = getBody(type, leadName);

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                logger.info("Email attempt {}/{}", attempt, MAX_RETRIES);

                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(fromEmail);
                helper.setTo(toEmail);
                helper.setSubject(subject);
                helper.setText(body, true); // true = HTML

                mailSender.send(message);

                logMessage(toEmail, subject, type, MessageStatus.SUCCESS, null);
                logger.info("Email sent successfully to: {}", toEmail);

                return new SendResponse(true, null, LocalDateTime.now());

            } catch (Exception e) {
                logger.warn("Email attempt {}/{} failed: {}", attempt, MAX_RETRIES, e.getMessage());

                if (attempt == MAX_RETRIES) {
                    logMessage(toEmail, subject, type, MessageStatus.FAILED, e.getMessage());
                    logger.error("All {} attempts failed for email: {}", MAX_RETRIES, toEmail);
                    throw new EmailException("Failed to send email after " + MAX_RETRIES + " retries", e);
                }

                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new EmailException("Retry interrupted", ie);
                }
            }
        }

        return null;
    }

    // ─── Subject by type ─────────────────────────────────────────────────────────

    private String getSubject(String type) {
        switch (type) {
            case "AUTO_NEW_LEAD": return "Welcome to LeadFlow!";
            case "REMINDER":     return "Checking in - LeadFlow";
            case "FOLLOWUP":     return "Follow-up - LeadFlow";
            case "MANUAL":       return "LeadFlow Update";
            default:             return "LeadFlow Update";
        }
    }

    // ─── Body by type ─────────────────────────────────────────────────────────────

    private String getBody(String type, String name) {
        switch (type) {
            case "AUTO_NEW_LEAD": return EmailTemplates.welcomeEmail(name);
            case "REMINDER":      return EmailTemplates.reminderEmail(name);
            case "FOLLOWUP":      return EmailTemplates.followupEmail(name);
            case "MANUAL":        return EmailTemplates.manualEmail(name);
            default:              return EmailTemplates.manualEmail(name);
        }
    }

    // ─── Private: Log to MongoDB ──────────────────────────────────────────────────

    private void logMessage(String email, String subject, String type,
                            MessageStatus status, String error) {
        try {
            MessageLog log = new MessageLog();
            log.setRecipient(email);
            log.setChannel("EMAIL");
            log.setMessageText(subject);
            log.setMessageType(MessageType.valueOf(type));
            log.setStatus(status);
            log.setErrorMessage(error);
            log.setSentAt(LocalDateTime.now());

            logger.info("Saving email log to MongoDB — status: {}", status);
            MessageLog saved = messageLogRepository.save(log);
            logger.info("Email log saved — id: {}", saved.getId());

        } catch (Exception e) {
            logger.error("Failed to save email log: {}", e.getMessage(), e);
        }
    }
}