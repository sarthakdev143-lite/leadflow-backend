package com.leadflow.leadflow_backend.repos;

import com.leadflow.leadflow_backend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository
        extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);
}