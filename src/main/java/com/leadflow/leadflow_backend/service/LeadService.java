package com.leadflow.leadflow_backend.service;

import com.leadflow.leadflow_backend.domain.Lead;
import com.leadflow.leadflow_backend.domain.LeadStatus;
import com.leadflow.leadflow_backend.model.LeadDTO;
import com.leadflow.leadflow_backend.repos.LeadRepository;
import com.leadflow.leadflow_backend.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LeadService {

    @Autowired
    private LeadRepository leadRepository;

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

    public List<LeadDTO> getAllLeads(String status) {
        String currentUserId = getCurrentUserId();
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

        if (!lead.getUserId().equals(getCurrentUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this lead");
        }

        return mapToDTO(lead, new LeadDTO());
    }

    public LeadDTO createLead(final LeadDTO leadDTO) {
        final Lead lead = new Lead();
        mapToEntity(leadDTO, lead);

        String currentUserId = getCurrentUserId();
        lead.setUserId(currentUserId);
        lead.setCreatedBy(currentUserId);

        lead.setCreatedAt(LocalDateTime.now());
        lead.setUpdatedAt(LocalDateTime.now());

        Lead savedLead = leadRepository.save(lead);
        return mapToDTO(savedLead, new LeadDTO());
    }

    public List<Lead> searchLeads(String query) {
        return leadRepository.findAll().stream()
                .filter(lead -> lead.getUserId() != null &&
                        lead.getUserId().equals(getCurrentUserId()) &&
                        (lead.getName().toLowerCase().contains(query.toLowerCase()) ||
                                lead.getEmail().toLowerCase().contains(query.toLowerCase())))
                .collect(Collectors.toList());
    }

    public Lead updateLead(String id, LeadDTO partialLead) {
        Lead existingLead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id));

        if (!existingLead.getUserId().equals(getCurrentUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this lead");
        }

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

    public void deleteLead(final String id) {
        Lead existingLead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id));

        if (!existingLead.getUserId().equals(getCurrentUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this lead");
        }

        leadRepository.deleteById(id);
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
    public boolean idExists(final String id) {
        return leadRepository.existsById(id);
    }
}