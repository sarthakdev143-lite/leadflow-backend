package com.leadflow.leadflow_backend.repos;
import com.leadflow.leadflow_backend.domain.Note;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NoteRepository extends MongoRepository<Note, String> {

    List<Note> findByUserIdOrderByCreatedAtDesc(String userId);
}