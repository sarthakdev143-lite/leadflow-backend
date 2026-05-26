package com.leadflow.leadflow_backend.rest;

import com.leadflow.leadflow_backend.teleException.EmailException;
import com.leadflow.leadflow_backend.model.Emailrequest;
import com.leadflow.leadflow_backend.model.SendResponse;
import com.leadflow.leadflow_backend.service.EmailService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private static final Logger logger = LoggerFactory.getLogger(EmailController.class);

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<SendResponse> sendEmail(
            @Valid @RequestBody Emailrequest request) {

        logger.info("Received email send request for: {} ({})", request.getName(), request.getEmail());

        try {
            SendResponse response = emailService.sendEmail(
                    request.getEmail(),
                    request.getName(),
                    request.getType()
            );
            return ResponseEntity.ok(response);

        } catch (EmailException e) {
            logger.error("EmailException: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new SendResponse(false, "Failed to send email"));

        } catch (Exception e) {
            logger.error("Unexpected error sending email: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SendResponse(false, "Internal server error"));
        }
    }
}
