package com.leadflow.leadflow_backend.service;
import com.leadflow.leadflow_backend.domain.Lead;
import com.leadflow.leadflow_backend.domain.LeadStatus;
import com.leadflow.leadflow_backend.model.LeadDTO;
import com.leadflow.leadflow_backend.repos.LeadRepository;
import com.leadflow.leadflow_backend.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
public class LeadService {

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TelegramService telegramService;

    private String getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        }

        if (principal instanceof com.leadflow.leadflow_backend.model.User) {
            return ((com.leadflow.leadflow_backend.model.User) principal).getEmail();
        }

        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public LeadDTO createLead(final LeadDTO leadDTO) {
        log.info("Creating new lead with name: {}", leadDTO.getName());
        final Lead lead = new Lead();
        mapToEntity(leadDTO, lead);

        String currentUserId = "yogeshhammad19@gmail.com";
        lead.setUserId(currentUserId);
        lead.setCreatedBy(currentUserId);

        lead.setCreatedAt(LocalDateTime.now());
        lead.setUpdatedAt(LocalDateTime.now());

        final Lead savedLead = leadRepository.save(lead);
        log.info("Lead structural insert committed in MongoDB with ID: {}", savedLead.getId());

        if (savedLead.getEmail() != null && !savedLead.getEmail().isBlank()) {
            try {
                emailService.sendEmail(
                        savedLead.getEmail(),
                        savedLead.getName(),
                        "AUTO_NEW_LEAD"
                );
                log.info("Welcome email sent to: {}", savedLead.getEmail());
            } catch (Exception e) {
                log.error("Failed to send welcome email to {}: {}", savedLead.getEmail(), e.getMessage());
            }
        }

        try {
            log.info("Handing over lead payload to background Telegram automation pipeline...");

            telegramService.sendMessage(
                    savedLead.getName() != null ? savedLead.getName() : "New Lead",
                    savedLead.getPhone() != null ? savedLead.getPhone() : "",
                    savedLead.getSource() != null ? savedLead.getSource() : "Direct",
                    "AUTO_NEW_LEAD",
                    "",
                    ""
            );

            log.info("Telegram background automation engine dispatched alert successfully!");
        } catch (Exception e) {
            log.error("Telegram automated alerting pipeline failed safely but database was protected: {}", e.getMessage());
        }

        return mapToDTO(savedLead, new LeadDTO());
    }

    public List<LeadDTO> getAllLeads(String status) {
        log.info("Fetching leads list. Filter status: {}", (status != null ? status : "ALL"));
        String currentUserId = "yogeshhammad19@gmail.com";
        List<Lead> leads;

        if (status != null && !status.isEmpty()) {
            leads = leadRepository.findByUserIdAndStatus(currentUserId, LeadStatus.valueOf(status));
        } else {
            leads = leadRepository.findByUserId(currentUserId);
        }

        return leads.stream()
                .map(lead -> mapToDTO(lead, new LeadDTO()))
                .collect(Collectors.toList());
    }

    public LeadDTO getLeadById(final String id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id));

        return mapToDTO(lead, new LeadDTO());
    }

    public Lead updateLead(String id, LeadDTO partialLead) {
        log.info("Processing partial update for lead ID: {}", id);
        Lead existingLead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id));

        if (partialLead.getName() != null && !partialLead.getName().isEmpty()) {
            existingLead.setName(partialLead.getName());
        }
        if (partialLead.getEmail() != null && !partialLead.getEmail().isEmpty()) {
            existingLead.setEmail(partialLead.getEmail());
        }
        if (partialLead.getPhone() != null && !partialLead.getPhone().isEmpty()) {
            existingLead.setPhone(partialLead.getPhone());
        }
        if (partialLead.getSource() != null) {
            existingLead.setSource(partialLead.getSource());
        }
        if (partialLead.getStatus() != null) {
            existingLead.setStatus(LeadStatus.valueOf(partialLead.getStatus()));
        }
        if (partialLead.getNotes() != null) {
            existingLead.setNotes(partialLead.getNotes());
        }

        existingLead.setUpdatedAt(LocalDateTime.now());
        return leadRepository.save(existingLead);
    }

    public List<Lead> searchLeads(String query) {
        log.info("Searching leads with query: {}", query);
        String currentUserId = "leadflow.officiall@gmail.com";
        List<Lead> leads;

        if (query == null || query.isEmpty()) {
            leads = leadRepository.findAll();
        } else if (query.matches("\\d+")) {
            leads = leadRepository.findByPhoneContaining(query);
        } else {
            leads = leadRepository.findByNameContainingIgnoreCase(query);
        }

        return leads.stream()
                .filter(lead -> lead.getUserId() != null && lead.getUserId().equals(currentUserId))
                .collect(Collectors.toList());
    }

    public void deleteLead(final String id) {
        log.warn("Processing execution command to drop lead ID: {}", id);
        Lead existingLead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id));

        leadRepository.deleteById(id);
        log.info("Lead completely dropped from MongoDB cluster collection. ID: {}", id);
    }

    public boolean idExists(final String id) {
        log.debug("Checking if lead exists for ID: {}", id);
        return leadRepository.existsById(id);
    }

    private LeadDTO mapToDTO(final Lead lead, final LeadDTO leadDTO) {
        leadDTO.setId(lead.getId());
        leadDTO.setName(lead.getName());
        leadDTO.setEmail(lead.getEmail());
        leadDTO.setPhone(lead.getPhone());
        leadDTO.setSource(lead.getSource());
        leadDTO.setStatus(lead.getStatus() == null ? null : lead.getStatus().name());
        leadDTO.setNotes(lead.getNotes());
        leadDTO.setUserId(lead.getUserId());
        leadDTO.setCreatedBy(lead.getCreatedBy());
        leadDTO.setCreatedAt(lead.getCreatedAt());
        leadDTO.setUpdatedAt(lead.getUpdatedAt());
        return leadDTO;
    }

    private Lead mapToEntity(final LeadDTO leadDTO, final Lead lead) {
        lead.setName(leadDTO.getName());
        lead.setEmail(leadDTO.getEmail());
        lead.setPhone(leadDTO.getPhone());
        lead.setSource(leadDTO.getSource());
        lead.setStatus(leadDTO.getStatus() == null ? null : LeadStatus.valueOf(leadDTO.getStatus()));
        lead.setNotes(leadDTO.getNotes());
        return lead;
    }
}
