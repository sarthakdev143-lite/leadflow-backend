package com.leadflow.leadflow_backend.Scheduler;
import com.leadflow.leadflow_backend.domain.Lead;
import com.leadflow.leadflow_backend.repos.LeadRepository;
import com.leadflow.leadflow_backend.service.AutomationService;
import com.leadflow.leadflow_backend.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class ScheduledTasks {

    private static final Logger logger =
            LoggerFactory.getLogger(ScheduledTasks.class);

    private final LeadRepository leadRepository;
    private final AutomationService automationService;
    private final EmailService emailService;

    public ScheduledTasks(
            LeadRepository leadRepository,
            AutomationService automationService,
            EmailService emailService
    ) {
        this.leadRepository = leadRepository;
        this.automationService = automationService;
        this.emailService = emailService;
    }

    // Runs every day at midnight (12:00 AM) to process automation rules
    @Scheduled(cron = "0 0 0 * * ?")
    public void runAutomation() {

        logger.info("=================================");
        logger.info("Automation Scheduler Started");
        logger.info("=================================");

        try {
            List<Lead> allLeads = leadRepository.findAll();
            logger.info("TOTAL LEADS IN DATABASE: {}", allLeads.size());

            sendReminders(allLeads);
            sendFollowups(allLeads);

        } catch (Exception e) {
            logger.error("Scheduler failed: {}", e.getMessage(), e);
        }

        logger.info("=================================");
        logger.info("Automation Scheduler Completed");
        logger.info("=================================");
    }

    // =====================================================
    // REMINDER: Triggers exactly 24 Hours after creation
    // =====================================================
    private void sendReminders(List<Lead> allLeads) {
        try {
            LocalDateTime now = LocalDateTime.now();
            int telegramSent = 0;
            int emailSent = 0;
            int skipped = 0;

            for (Lead lead : allLeads) {
                // Condition: Status is NEW, hasn't received reminder, and created at least 24 hours ago
                if ("NEW".equalsIgnoreCase(String.valueOf(lead.getStatus()))
                        && lead.getCreatedAt() != null
                        && lead.getCreatedAt().isBefore(now.minusHours(24))
                        && lead.getLastReminderSent() == null) {

                    logger.info("Processing NEW lead for 24-hour reminder: {}", lead.getName());

                    // Telegram Execution
                    try {
                        automationService.sendTelegramNotification(lead, "REMINDER");
                        telegramSent++;
                    } catch (Exception e) {
                        logger.error("Telegram reminder failed for {} : {}", lead.getName(), e.getMessage());
                    }

                    // Email Execution
                    if (lead.getEmail() != null && !lead.getEmail().trim().isEmpty()) {
                        try {
                            emailService.sendEmail(
                                    lead.getEmail(),
                                    "REMINDER",
                                    "Hello " + lead.getName() + ", this is your reminder."
                            );
                            emailSent++;
                        } catch (Exception e) {
                            logger.error("Email reminder failed for {} : {}", lead.getName(), e.getMessage());
                        }
                    } else {
                        skipped++;
                    }

                    lead.setLastReminderSent(LocalDateTime.now());
                    leadRepository.save(lead);
                    logger.info("Updated reminder state for: {}", lead.getName());
                }
            }

            logger.info("Reminder Summary -> Telegram Sent: {}, Email Sent: {}", telegramSent, emailSent);

        } catch (Exception e) {
            logger.error("Error inside sendReminders(): {}", e.getMessage());
        }
    }

    // =====================================================
    // FOLLOWUP: Triggers exactly 2 Days (48 Hours) after creation
    // =====================================================
    private void sendFollowups(List<Lead> allLeads) {
        try {
            LocalDateTime now = LocalDateTime.now();
            int telegramSent = 0;
            int emailSent = 0;
            int skipped = 0;

            for (Lead lead : allLeads) {
                // Condition: Created at least 2 days ago and hasn't received follow-up yet
                if (lead.getCreatedAt() != null
                        && lead.getCreatedAt().isBefore(now.minusDays(2))
                        && lead.getLastFollowupSent() == null) {

                    logger.info("Processing lead for 2-day follow-up: {}", lead.getName());

                    // Telegram Execution
                    try {
                        automationService.sendTelegramNotification(lead, "FOLLOWUP");
                        telegramSent++;
                    } catch (Exception e) {
                        logger.error("Telegram follow-up failed for {} : {}", lead.getName(), e.getMessage());
                    }

                    // Email Execution
                    if (lead.getEmail() != null && !lead.getEmail().trim().isEmpty()) {
                        try {
                            emailService.sendEmail(
                                    lead.getEmail(),
                                    "FOLLOWUP",
                                    "Hello " + lead.getName() + ", this is your follow-up reminder."
                            );
                            emailSent++;
                        } catch (Exception e) {
                            logger.error("Email follow-up failed for {} : {}", lead.getName(), e.getMessage());
                        }
                    } else {
                        skipped++;
                    }

                    lead.setLastFollowupSent(LocalDateTime.now());
                    leadRepository.save(lead);
                    logger.info("Updated follow-up state for: {}", lead.getName());
                }
            }

            logger.info("Follow-up Summary -> Telegram Sent: {}, Email Sent: {}", telegramSent, emailSent);

        } catch (Exception e) {
            logger.error("Error inside sendFollowups(): {}", e.getMessage());
        }
    }
}