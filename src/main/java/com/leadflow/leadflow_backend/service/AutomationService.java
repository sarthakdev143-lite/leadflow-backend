package com.leadflow.leadflow_backend.service;

import com.leadflow.leadflow_backend.domain.Lead;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

    @Service
    public class AutomationService {

        private static final Logger logger =
                LoggerFactory.getLogger(AutomationService.class);

        private final EmailService emailService;

        // Constructor Injection
        public AutomationService(EmailService emailService) {
            this.emailService = emailService;
        }

        // =========================================
        // SEND TELEGRAM NOTIFICATION
        // =========================================
        public void sendTelegramNotification(
                Lead lead,
                String type
        ) {

            try {

                logger.info(
                        "Telegram {} notification sent for lead: {}",
                        type,
                        lead.getName()
                );

            } catch (Exception e) {

                logger.error(
                        "Failed Telegram notification for {} : {}",
                        lead.getName(),
                        e.getMessage()
                );
            }
        }

        // =========================================
        // SEND REMINDER EMAIL
        // =========================================
        public void sendReminderEmail(Lead lead) {

            try {

                // Skip if no email
                if (lead.getEmail() == null ||
                        lead.getEmail().isBlank()) {

                    logger.warn(
                            "Lead {} has no email address",
                            lead.getName()
                    );

                    return;
                }

                emailService.sendEmail(
                        lead.getEmail(),
                        "Lead Reminder",
                        "Hello " + lead.getName()
                                + ", this is your reminder message."
                );

                logger.info(
                        "Reminder email sent to: {}",
                        lead.getEmail()
                );

            } catch (Exception e) {

                logger.error(
                        "Failed to send reminder email to {} : {}",
                        lead.getName(),
                        e.getMessage()
                );
            }
        }

        // =========================================
        // SEND FOLLOW-UP EMAIL
        // =========================================
        public void sendFollowupEmail(Lead lead) {

            try {

                // Skip if no email
                if (lead.getEmail() == null ||
                        lead.getEmail().isBlank()) {

                    logger.warn(
                            "Lead {} has no email address",
                            lead.getName()
                    );

                    return;
                }

                emailService.sendEmail(
                        lead.getEmail(),
                        "Lead Follow-up",
                        "Hello " + lead.getName()
                                + ", this is your follow-up reminder."
                );

                logger.info(
                        "Follow-up email sent to: {}",
                        lead.getEmail()
                );

            } catch (Exception e) {

                logger.error(
                        "Failed to send follow-up email to {} : {}",
                        lead.getName(),
                        e.getMessage()
                );
            }
        }
    }
