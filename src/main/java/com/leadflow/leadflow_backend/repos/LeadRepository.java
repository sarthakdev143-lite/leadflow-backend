package com.leadflow.leadflow_backend.repos;

import com.leadflow.leadflow_backend.domain.Lead;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface LeadRepository extends MongoRepository<Lead, String> {

    boolean existsByIdIgnoreCase(String id);

}
