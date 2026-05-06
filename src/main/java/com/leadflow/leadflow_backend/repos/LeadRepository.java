package com.leadflow.leadflow_backend.repos;

import com.leadflow.leadflow_backend.domain.Lead;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;


public interface LeadRepository extends MongoRepository<Lead, String> {
    List<Lead> findByStatusAndCreatedAtBefore (String status, LocalDateTime threshold);

    boolean existsByIdIgnoreCase(String id);

}
