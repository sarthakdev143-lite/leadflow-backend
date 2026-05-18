package com.leadflow.leadflow_backend.repos;

import com.leadflow.leadflow_backend.domain.Lead;
import com.leadflow.leadflow_backend.domain.LeadStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeadRepository extends MongoRepository<Lead, String> {

    List<Lead> findByStatus(LeadStatus status);
    List<Lead> findByNameContainingIgnoreCase(String name);
    List<Lead> findByPhoneContaining(String phone);


    @Query("{ 'status': 'NEW', 'createdAt': { $lt: ?0 } }")
    List<Lead> findLeadsNeedingReminder(LocalDateTime cutoff24h);
    @Query("{ 'status': 'IN_PROGRESS', 'updatedAt': { $lt: ?0 } }")
    List<Lead> findLeadsNeedingFollowup(LocalDateTime cutoff2d);

    List<Lead> findByUserId(String userId);
    List<Lead> findByUserIdAndStatus(String userId, LeadStatus status);

}