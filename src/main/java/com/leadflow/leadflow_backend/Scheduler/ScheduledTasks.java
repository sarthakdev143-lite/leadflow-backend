package com.leadflow.leadflow_backend.Scheduler;

import com.leadflow.leadflow_backend.domain.Lead;
import com.leadflow.leadflow_backend.repos.LeadRepository;
import com.leadflow.leadflow_backend.service.AutomationService;
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

    // Every 2 minutes
    @Scheduled(cron = "0 */2 * * * *")
    public void runAutomation() {

        logger.info("=================================");
        logger.info("Automation Scheduler Started");
        logger.info("=================================");

        try {

            // DEBUG DATABASE COUNT
            List<Lead> allLeads = leadRepository.findAll();

            logger.info("TOTAL LEADS IN DATABASE: {}", allLeads.size());

            for (Lead lead : allLeads) {

                logger.info(
                        "Lead -> Name: {}, Status: {}, CreatedAt: {}",
                        lead.getName(),
                        lead.getStatus(),
                        lead.getCreatedAt()
                );
            }

            sendReminders();

            sendFollowups();

        } catch (Exception e) {

            logger.error(
                    "Scheduler failed: {}",
                    e.getMessage(),
                    e
            );
        }

        logger.info("=================================");
        logger.info("Automation Scheduler Completed");
        logger.info("=================================");
    }

    // =====================================================
    // NEW LEADS REMINDER
    // =====================================================

    private void sendReminders() {

        try {

            LocalDateTime cutoff24h =
                    LocalDateTime.now().minusHours(24);

            logger.info("Reminder cutoff time: {}", cutoff24h);

            List<Lead> newLeads =
                    leadRepository.findLeadsNeedingReminder(cutoff24h);

            logger.info(
                    "Total NEW leads found: {}",
                    newLeads.size()
            );

            int telegramSent = 0;
            int emailSent = 0;
            int skipped = 0;

            for (Lead lead : newLeads) {

                logger.info(
                        "Processing NEW lead: {}",
                        lead.getName()
                );

                // TELEGRAM
                try {

                    automationService.sendTelegramNotification(
                            lead,
                            "REMINDER"
                    );

                    telegramSent++;

                    logger.info(
                            "Telegram reminder sent for lead: {}",
                            lead.getName()
                    );

                } catch (Exception e) {

                    logger.error(
                            "Telegram reminder failed for {} : {}",
                            lead.getName(),
                            e.getMessage()
                    );
                }

                // EMAIL
                if (lead.getEmail() != null &&
                        !lead.getEmail().trim().isEmpty()) {

                    try {

                        emailService.sendEmail(
                                lead.getEmail(),
                                "Lead Reminder",
                                "Hello " + lead.getName()
                                        + ", this is your reminder."
                        );

                        emailSent++;

                        logger.info(
                                "Email reminder sent to: {}",
                                lead.getEmail()
                        );

                    } catch (Exception e) {

                        logger.error(
                                "Email reminder failed for {} : {}",
                                lead.getName(),
                                e.getMessage()
                        );
                    }

                } else {

                    skipped++;

                    logger.info(
                            "No email found for lead: {}",
                            lead.getName()
                    );
                }

                // UPDATE TIMESTAMP
                lead.setLastReminderSent(
                        LocalDateTime.now()
                );

                leadRepository.save(lead);

                logger.info(
                        "Updated reminder timestamp for lead: {}",
                        lead.getName()
                );
            }

            logger.info(
                    "Reminder Summary -> Telegram: {}, Email: {}, Skipped: {}, Total: {}",
                    telegramSent,
                    emailSent,
                    skipped,
                    newLeads.size()
            );

        } catch (Exception e) {

            logger.error(
                    "Error inside sendReminders(): {}",
                    e.getMessage(),
                    e
            );
        }
    }

    // =====================================================
    // CONTACTED LEADS FOLLOW-UP
    // =====================================================

    private void sendFollowups() {

        try {

            LocalDateTime cutoff2d =
                    LocalDateTime.now().minusDays(2);

            logger.info("Follow-up cutoff time: {}", cutoff2d);

            List<Lead> contactedLeads =
                    leadRepository.findLeadsNeedingFollowup(cutoff2d);

            logger.info(
                    "Total CONTACTED leads found: {}",
                    contactedLeads.size()
            );

            int telegramSent = 0;
            int emailSent = 0;
            int skipped = 0;

            for (Lead lead : contactedLeads) {

                logger.info(
                        "Processing CONTACTED lead: {}",
                        lead.getName()
                );

                // TELEGRAM
                try {

                    automationService.sendTelegramNotification(
                            lead,
                            "FOLLOWUP"
                    );

                    telegramSent++;

                    logger.info(
                            "Telegram follow-up sent for lead: {}",
                            lead.getName()
                    );

                } catch (Exception e) {

                    logger.error(
                            "Telegram follow-up failed for {} : {}",
                            lead.getName(),
                            e.getMessage()
                    );
                }

                // EMAIL
                if (lead.getEmail() != null &&
                        !lead.getEmail().trim().isEmpty()) {

                    try {

                        emailService.sendEmail(
                                lead.getEmail(),
                                "Follow-up Reminder",
                                "Hello " + lead.getName()
                                        + ", this is your follow-up reminder."
                        );

                        emailSent++;

                        logger.info(
                                "Email follow-up sent to: {}",
                                lead.getEmail()
                        );

                    } catch (Exception e) {

                        logger.error(
                                "Email follow-up failed for {} : {}",
                                lead.getName(),
                                e.getMessage()
                        );
                    }

                } else {

                    skipped++;

                    logger.info(
                            "No email found for lead: {}",
                            lead.getName()
                    );
                }

                // UPDATE TIMESTAMP
                lead.setLastFollowupSent(
                        LocalDateTime.now()
                );

                leadRepository.save(lead);

                logger.info(
                        "Updated follow-up timestamp for lead: {}",
                        lead.getName()
                );
            }

            logger.info(
                    "Follow-up Summary -> Telegram: {}, Email: {}, Skipped: {}, Total: {}",
                    telegramSent,
                    emailSent,
                    skipped,
                    contactedLeads.size()
            );

        } catch (Exception e) {

            logger.error(
                    "Error inside sendFollowups(): {}",
                    e.getMessage(),
                    e
            );
        }
    }
}
