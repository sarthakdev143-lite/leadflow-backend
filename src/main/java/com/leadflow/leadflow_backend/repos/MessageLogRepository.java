package com.leadflow.leadflow_backend.repos;

import com.leadflow.leadflow_backend.domain.MessageLog;
import com.leadflow.leadflow_backend.domain.MessageStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageLogRepository extends MongoRepository<MessageLog, String> {

    List<MessageLog> findByStatus(MessageStatus status);

    List<MessageLog> findByChatId(String chatId);
}
