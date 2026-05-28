package com.leadflow.leadflow_backend.rest;

import com.leadflow.leadflow_backend.model.ManualMessageRequest;
import com.leadflow.leadflow_backend.model.SendResponse;
import com.leadflow.leadflow_backend.service.TelegramService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/automation")
@CrossOrigin(origins = "*", allowedHeaders = "*") // 🎯 CORS Error safe layout
public class ManualMessageResource {

    private static final Logger log = LoggerFactory.getLogger(ManualMessageResource.class);

    @Autowired
    private TelegramService telegramService;

    @PostMapping("/manual-message")
    public ResponseEntity<?> sendManualLeadMessage(@RequestBody ManualMessageRequest request) {
        log.info("Received manual message request via UI dashboard for Chat ID: {}", request.getChatId());

        try {
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Message text content cannot be empty");
            }

            SendResponse response = telegramService.sendManualCustomMessage(
                    request.getChatId(),
                    request.getMessage()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error executing dynamic manual dashboard dispatch: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to deliver manual text message: " + e.getMessage());
        }
    }
}