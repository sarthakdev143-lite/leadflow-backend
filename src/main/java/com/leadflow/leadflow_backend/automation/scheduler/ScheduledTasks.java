/* package com.leadflow.leadflow_backend.automation.scheduler;

    import com.leadflow.leadflow_backend.domain.Lead;
    import com.leadflow.leadflow_backend.repos.LeadRepository;
    import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

    @Component
    public class ScheduledTasks {

        private final LeadRepository leadRepository;
        public ScheduledTasks(LeadRepository leadRepository) {
            this.leadRepository = leadRepository;
        }

        @Scheduled(fixedRate = 60000)
        public void processLeads() {

            LocalDateTime now = LocalDateTime.now();

            // -------------------------------
            // Rule 1: NEW leads > 24 hours
            // -------------------------------
            LocalDateTime newThreshold = now.minusHours(1);

            List<Lead> newLeads = leadRepository
                    .findByStatusAndCreatedAtBefore("NEW", newThreshold);

            for (Lead lead : newLeads) {

                // ✅ Avoid duplicate reminder
                if (lead.getLastReminderSent() == null) {

                    System.out.println("Reminder sent to NEW Lead: " + lead.getName());

                    lead.setLastReminderSent(now);
                    leadRepository.save(lead);
                }
            }

            // -------------------------------
            // Rule 2: CONTACTED leads > 2 days
            // -------------------------------
            LocalDateTime contactedThreshold = now.minusDays(2);

            List<Lead> contactedLeads = leadRepository
                    .findByStatusAndCreatedAtBefore("CONTACTED", contactedThreshold);

            for (Lead lead : contactedLeads) {

                // ✅ Avoid duplicate follow-up
                if (lead.getLastFollowupSent() == null) {

                    System.out.println("Follow-up sent to CONTACTED Lead: " + lead.getName());

                    lead.setLastFollowupSent(now);
                    leadRepository.save(lead);
                }
            }
        }

        @Scheduled(fixedRate = 60000) // runs every 1 minute (for testing)
        public void sendFollowUpReminders() {

            LocalDateTime threshold = LocalDateTime.now().minusHours(24);

            List<Lead> leads = leadRepository.findByStatusAndCreatedAtBefore("PENDING", threshold);

            for (Lead lead : leads) {
                // Simulate sending reminder
                System.out.println("Reminder sent to Lead: " + lead.getName());

                // Optional: update status to avoid duplicate reminders
                lead.setStatus("FOLLOWED_UP");
                leadRepository.save(lead);

                // -------------------------------
                // Rule 2: CONTACTED leads > 2 days
                // -------------------------------
                LocalDateTime contactedThreshold = LocalDateTime.now().minusDays(2);

                List<Lead> contactedLeads = leadRepository
                        .findByStatusAndCreatedAtBefore("CONTACTED", contactedThreshold);

                for (Lead contactedlead : contactedLeads) {
                    System.out.println("Follow-up sent to CONTACTED Lead: " + contactedlead.getName());

                    // update status
                    contactedlead.setStatus("FOLLOWED_UP");
                    leadRepository.save(contactedlead);
                    System.out.println("Scheduler is running...");
                }
            }
        }
    }

 */
package com.leadflow.leadflow_backend.automation.scheduler;

import com.leadflow.leadflow_backend.domain.Lead;
import com.leadflow.leadflow_backend.repos.LeadRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ScheduledTasks {

    private final LeadRepository leadRepository;

    public ScheduledTasks(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    @Scheduled(fixedRate = 60000) // every 1 min
    public void processLeads() {

        System.out.println("Scheduler running...");

        LocalDateTime now = LocalDateTime.now();

        // -------------------------------
        // Rule 1: NEW leads > 1 hour (for testing)
        // -------------------------------
        LocalDateTime newThreshold = now.minusHours(24);

        List<Lead> newLeads = leadRepository
                .findByStatusAndCreatedAtBefore("NEW", newThreshold);

        for (Lead lead : newLeads) {

            if (lead.getLastReminderSent() == null) {

                System.out.println("Reminder sent to NEW Lead: " + lead.getName());

                lead.setLastReminderSent(now);
                leadRepository.save(lead);
            }
        }

        // -------------------------------
        // Rule 2: CONTACTED leads > 2 days
        // -------------------------------
        LocalDateTime contactedThreshold = now.minusDays(2);

        List<Lead> contactedLeads = leadRepository
                .findByStatusAndCreatedAtBefore("CONTACTED", contactedThreshold);

        for (Lead lead : contactedLeads) {

            if (lead.getLastFollowupSent() == null) {

                System.out.println("Follow-up sent to CONTACTED Lead: " + lead.getName());

                lead.setLastFollowupSent(now);
                leadRepository.save(lead);
            }
        }
    }
}
