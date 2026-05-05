package com.leadflow.leadflow_backend.repos;

import com.leadflow.leadflow_backend.model.Lead;
import com.leadflow.leadflow_backend.model.LeadStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadRepository extends MongoRepository<Lead, String> {

    List<Lead> findByStatus(LeadStatus status);
}