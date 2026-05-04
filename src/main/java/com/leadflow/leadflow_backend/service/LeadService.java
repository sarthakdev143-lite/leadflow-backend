package com.leadflow.leadflow_backend.service;

import com.leadflow.leadflow_backend.domain.Lead;
import com.leadflow.leadflow_backend.model.LeadDTO;
import com.leadflow.leadflow_backend.repos.LeadRepository;
import com.leadflow.leadflow_backend.util.NotFoundException;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class LeadService {

    private final LeadRepository leadRepository;

    public LeadService(final LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    public List<LeadDTO> findAll() {
        final List<Lead> leads = leadRepository.findAll(Sort.by("id"));
        return leads.stream()
                .map(lead -> mapToDTO(lead, new LeadDTO()))
                .toList();
    }

    public LeadDTO get(final String id) {
        return leadRepository.findById(id)
                .map(lead -> mapToDTO(lead, new LeadDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public String create(final LeadDTO leadDTO) {
        final Lead lead = new Lead();
        mapToEntity(leadDTO, lead);
        lead.setId(leadDTO.getId());
        return leadRepository.save(lead).getId();
    }

    public void update(final String id, final LeadDTO leadDTO) {
        final Lead lead = leadRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(leadDTO, lead);
        leadRepository.save(lead);
    }

    public void delete(final String id) {
        final Lead lead = leadRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        leadRepository.delete(lead);
    }

    private LeadDTO mapToDTO(final Lead lead, final LeadDTO leadDTO) {
        leadDTO.setId(lead.getId());
        leadDTO.setUserId(lead.getUserId());
        leadDTO.setName(lead.getName());
        leadDTO.setPhone(lead.getPhone());
        leadDTO.setEmail(lead.getEmail());
        leadDTO.setSource(lead.getSource());
        leadDTO.setStatus(lead.getStatus());
        leadDTO.setNotes(lead.getNotes());
        leadDTO.setLastContacted(lead.getLastContacted());
        leadDTO.setCreatedAt(lead.getCreatedAt());
        leadDTO.setUpdatedAt(lead.getUpdatedAt());
        return leadDTO;
    }

    private Lead mapToEntity(final LeadDTO leadDTO, final Lead lead) {
        lead.setUserId(leadDTO.getUserId());
        lead.setName(leadDTO.getName());
        lead.setPhone(leadDTO.getPhone());
        lead.setEmail(leadDTO.getEmail());
        lead.setSource(leadDTO.getSource());
        lead.setStatus(leadDTO.getStatus());
        lead.setNotes(leadDTO.getNotes());
        lead.setLastContacted(leadDTO.getLastContacted());
        lead.setCreatedAt(leadDTO.getCreatedAt());
        lead.setUpdatedAt(leadDTO.getUpdatedAt());
        return lead;
    }

    public boolean idExists(final String id) {
        return leadRepository.existsByIdIgnoreCase(id);
    }

}
