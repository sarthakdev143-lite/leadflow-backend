package com.leadflow.leadflow_backend.rest;

import com.leadflow.leadflow_backend.model.LeadDTO;
import com.leadflow.leadflow_backend.service.LeadService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/leads", produces = MediaType.APPLICATION_JSON_VALUE)
public class LeadResource {

    private final LeadService leadService;

    public LeadResource(final LeadService leadService) {
        this.leadService = leadService;
    }

    @GetMapping
    public ResponseEntity<List<LeadDTO>> getAllLeads() {
        return ResponseEntity.ok(leadService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeadDTO> getLead(@PathVariable(name = "id") final String id) {
        return ResponseEntity.ok(leadService.get(id));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<String> createLead(@RequestBody @Valid final LeadDTO leadDTO) {
        final String createdId = leadService.create(leadDTO);
        return new ResponseEntity<>('"' + createdId + '"', HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateLead(@PathVariable(name = "id") final String id,
            @RequestBody @Valid final LeadDTO leadDTO) {
        leadService.update(id, leadDTO);
        return ResponseEntity.ok('"' + id + '"');
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteLead(@PathVariable(name = "id") final String id) {
        leadService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
