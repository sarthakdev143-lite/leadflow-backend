package com.leadflow.leadflow_backend.rest;

import com.leadflow.leadflow_backend.domain.Lead;
import com.leadflow.leadflow_backend.model.LeadDTO;
import com.leadflow.leadflow_backend.service.LeadService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/leads")
public class LeadResource {

    @Autowired
    private LeadService leadService;

    @PostMapping
    public ResponseEntity<?> createLead(@Valid @RequestBody final LeadDTO leadDTO) {
        log.info("REST request to create a new lead: {}", leadDTO.getName());
        try {
            LeadDTO created = leadService.createLead(leadDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("Error creating lead: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating lead: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllLeads(@RequestParam(required = false) String status) {
        log.info("REST request to get all leads with status: {}", status);
        try {
            return ResponseEntity.ok(leadService.getAllLeads(status));
        } catch (Exception e) {
            log.error("Failed to fetch leads: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLeadById(@PathVariable final String id) {
        log.info("REST request to get lead by ID: {}", id);
        try {
            return ResponseEntity.ok(leadService.getLeadById(id));
        } catch (ResponseStatusException e) {
            log.error("Security violation for ID {}: {}", id, e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (RuntimeException e) {
            log.warn("Lead not found for ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Lead>> search(@RequestParam String query) {
        log.info("REST request to search leads with query: {}", query);
        return ResponseEntity.ok(leadService.searchLeads(query));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLead(@PathVariable final String id, @RequestBody final LeadDTO leadDTO) {
        log.info("REST request to update lead ID: {}", id);
        try {
            return ResponseEntity.ok(leadService.updateLead(id, leadDTO));
        } catch (ResponseStatusException e) {
            log.error("Security violation for ID {}: {}", id, e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (RuntimeException e) {
            log.error("Update failed for ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLead(@PathVariable final String id) {
        log.warn("REST request to delete lead with ID: {}", id);
        try {
            leadService.deleteLead(id);
            return ResponseEntity.ok("Lead deleted successfully.");
        } catch (ResponseStatusException e) {
            log.error("Security violation on delete for ID {}: {}", id, e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (RuntimeException e) {
            log.error("Delete failed for ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}