package com.leadflow.leadflow_backend.repos;

import com.leadflow.leadflow_backend.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface UserRepository extends MongoRepository<User, String> {

    boolean existsByIdIgnoreCase(String id);

}
