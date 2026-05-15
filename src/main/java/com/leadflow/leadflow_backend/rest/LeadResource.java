package com.leadflow.leadflow_backend.rest;

import com.leadflow.leadflow_backend.domain.Lead;
import com.leadflow.leadflow_backend.exception.ResourceNotFoundException;
import com.leadflow.leadflow_backend.model.LeadDTO;
import com.leadflow.leadflow_backend.service.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadResource {

    private final LeadService leadService;

    @PostMapping
    public ResponseEntity<LeadDTO> createLead(@Valid @RequestBody final LeadDTO leadDTO) {
        log.info("REST request to create a new lead: {}", leadDTO.getName());
        LeadDTO created = leadService.createLead(leadDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<LeadDTO>> getAllLeads(@RequestParam(required = false) String status) {
        log.info("REST request to get all leads with status: {}", status);
        return ResponseEntity.ok(leadService.getAllLeads(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeadDTO> getLeadById(@PathVariable final String id) {
        log.info("REST request to get lead by ID: {}", id);
        return ResponseEntity.ok(leadService.getLeadById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Lead>> search(@RequestParam String query) {
        log.info("REST request to search leads with query: {}", query);
        return ResponseEntity.ok(leadService.searchLeads(query));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeadDTO> updateLead(@PathVariable final String id, @RequestBody final LeadDTO leadDTO) {
        log.info("REST request to update lead ID: {}", id);
        return ResponseEntity.ok(leadService.updateLead(id, leadDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLead(@PathVariable final String id) {
        log.warn("REST request to delete lead with ID: {}", id);
        leadService.deleteLead(id);
        return ResponseEntity.noContent().build();
    }
}