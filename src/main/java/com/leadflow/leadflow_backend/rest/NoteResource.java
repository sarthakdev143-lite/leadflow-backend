package com.leadflow.leadflow_backend.rest;

import com.leadflow.leadflow_backend.domain.Note;
import com.leadflow.leadflow_backend.service.NoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/notes")
public class NoteResource {

    @Autowired
    private NoteService noteService;

    private String extractUsername(Authentication authentication) {
        if (authentication == null) {
            return "anonymous";
        }
        Object principal = authentication.getPrincipal();
        if (principal == null) {
            return authentication.getName();
        }

        try {
            // Try extracting using standard reflection to match any custom domain User model (getId or getEmail)
            Method getIdMethod = principal.getClass().getMethod("getId");
            Object idVal = getIdMethod.invoke(principal);
            if (idVal != null) return idVal.toString();
        } catch (Exception e1) {
            try {
                Method getEmailMethod = principal.getClass().getMethod("getEmail");
                Object emailVal = getEmailMethod.invoke(principal);
                if (emailVal != null) return emailVal.toString();
            } catch (Exception e2) {
                log.warn("Could not explicitly extract fields via reflection, falling back to clean string parsing.");
            }
        }

        // Ultimate fallback parsing if toString returns com.package.User@hash
        String strRepr = principal.toString();
        if (strRepr.contains("@")) {
            return authentication.getName();
        }
        return strRepr;
    }

    @PostMapping
    public ResponseEntity<?> createNote(@RequestBody Map<String, String> request, Authentication authentication) {
        log.info("REST request to create a new note");
        try {
            String cleanUserIdentifier = extractUsername(authentication);
            String content = request.get("content");

            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Content cannot be empty");
            }

            Note created = noteService.createNote(cleanUserIdentifier, cleanUserIdentifier, content);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("Error creating note: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating note: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserNotes(Authentication authentication) {
        log.info("REST request to fetch all notes for logged-in user");
        try {
            String cleanUserIdentifier = extractUsername(authentication);
            return ResponseEntity.ok(noteService.getNotesByUser(cleanUserIdentifier));
        } catch (Exception e) {
            log.error("Failed to fetch notes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateNote(@PathVariable final String id, @RequestBody Map<String, String> request) {
        log.info("REST request to update note ID: {}", id);
        try {
            String newContent = request.get("content");
            if (newContent == null || newContent.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Content cannot be empty");
            }

            Note updated = noteService.updateNote(id, newContent);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("Update failed for note ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error updating note ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNote(@PathVariable final String id) {
        log.warn("REST request to delete note with ID: {}", id);
        try {
            noteService.deleteNote(id);
            return ResponseEntity.ok("Note deleted successfully.");
        } catch (RuntimeException e) {
            log.error("Delete failed for note ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}