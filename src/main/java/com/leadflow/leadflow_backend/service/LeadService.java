package com.leadflow.leadflow_backend.service;

import com.leadflow.leadflow_backend.domain.Lead;
import com.leadflow.leadflow_backend.domain.LeadStatus;
import com.leadflow.leadflow_backend.exception.ResourceNotFoundException;
import com.leadflow.leadflow_backend.model.LeadDTO;
import com.leadflow.leadflow_backend.repos.LeadRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LeadService {

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AutomationService automationService;

    public LeadDTO createLead(final LeadDTO leadDTO) {
        log.info("Creating new lead with name: {}", leadDTO.getName());
        final Lead lead = new Lead();
        mapToEntity(leadDTO, lead);

        lead.setStatus(LeadStatus.NEW);
        lead.setCreatedAt(LocalDateTime.now());
        lead.setUpdatedAt(LocalDateTime.now());

        final Lead savedLead = leadRepository.save(lead);
        log.info("Lead saved with ID: {}", savedLead.getId());

        // Send email notification for new lead
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

        automationService.sendTelegramNotification(savedLead, "AUTO_NEW_LEAD");

        return mapToDTO(savedLead, new LeadDTO());
    }

    public List<LeadDTO> getAllLeads(String statusStr) {
        log.info("Fetching leads list. Filter status: {}", (statusStr != null ? statusStr : "ALL"));
        List<Lead> leads;
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            try {
                LeadStatus status = LeadStatus.valueOf(statusStr.toUpperCase());
                leads = leadRepository.findByStatus(status);
            } catch (IllegalArgumentException e) {
                log.error("Invalid status received: {}. Falling back to all leads.", statusStr);
                leads = leadRepository.findAll();
            }
        } else {
            leads = leadRepository.findAll();
        }

        return leads.stream()
                .map(lead -> mapToDTO(lead, new LeadDTO()))
                .collect(Collectors.toList());
    }

    public LeadDTO getLeadById(final String id) {
        log.info("Fetching lead details for ID: {}", id);

        final Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + id));
        return mapToDTO(lead, new LeadDTO());
    }

    public Lead updateLead(String id, @Valid LeadDTO partialLead) {
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

        log.info("Lead ID: {} updated successfully", id);
        return leadRepository.save(existingLead);
    }

    public List<Lead> searchLeads(String query) {
        log.info("Searching leads with query: {}", query);
        if (query == null || query.isEmpty()) {
            return leadRepository.findAll();
        }
        if (query.matches("\\d+")) {
            return leadRepository.findByPhoneContaining(query);
        }
        return leadRepository.findByNameContainingIgnoreCase(query);
    }

    public void deleteLead(final String id) {
        log.warn("Deleting lead with ID: {}", id);

        if (!leadRepository.existsById(id)) {
            throw new ResourceNotFoundException("Lead not found with ID: " + id);
        }
        leadRepository.deleteById(id);
    }

    public boolean idExists(final String id) {
        log.debug("Checking if lead exists for ID: {}", id);
        return leadRepository.existsById(id);
    }

    private Lead mapToEntity(final LeadDTO leadDTO, final Lead lead) {
        lead.setName(leadDTO.getName());
        lead.setEmail(leadDTO.getEmail());
        lead.setPhone(leadDTO.getPhone());
        lead.setSource(leadDTO.getSource());
        lead.setNotes(leadDTO.getNotes());
        if (leadDTO.getStatus() != null) {
            lead.setStatus(LeadStatus.valueOf(leadDTO.getStatus()));
        }
        return lead;
    }

    private LeadDTO mapToDTO(final Lead lead, final LeadDTO leadDTO) {
        leadDTO.setId(lead.getId());
        leadDTO.setName(lead.getName());
        leadDTO.setPhone(lead.getPhone());
        leadDTO.setSource(lead.getSource());
        leadDTO.setStatus(lead.getStatus().name());
        leadDTO.setNotes(lead.getNotes());
        if (lead.getCreatedAt() != null) {
            leadDTO.setCreatedAt(lead.getCreatedAt().atOffset(ZoneOffset.UTC).toLocalDateTime());
        }
        return leadDTO;
    }
}
