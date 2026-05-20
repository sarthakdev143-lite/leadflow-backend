package com.leadflow.leadflow_backend.service;

import com.leadflow.leadflow_backend.domain.Note;
import com.leadflow.leadflow_backend.repos.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    public Note createNote(String userId, String createdBy, String content) {
        Note note = new Note();
        note.setUserId(userId);
        note.setCreatedBy(createdBy);
        note.setContent(content);
        note.setCreatedAt(LocalDateTime.now());
        return noteRepository.save(note);
    }

    public List<Note> getNotesByUser(String userId) {
        return noteRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Note updateNote(String noteId, String newContent) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found with ID: " + noteId));
        note.setContent(newContent);
        return noteRepository.save(note);
    }

    public void deleteNote(String noteId) {
        noteRepository.deleteById(noteId);
    }
}