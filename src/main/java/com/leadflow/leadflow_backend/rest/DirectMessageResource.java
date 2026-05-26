package com.leadflow.leadflow_backend.rest;

import com.leadflow.leadflow_backend.model.DirectMessagePayload; // Model class ka import path
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/automation")
public class DirectMessageResource {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.whatsapp.number}")
    private String twilioWhatsappNumber;
    @PostMapping("/direct-whatsapp")
    public ResponseEntity<?> sendDirectMessage(@RequestBody final DirectMessagePayload payload) {
        log.info("Initiating direct background dispatch to number: {}", payload.getPhone());
        try {
            Twilio.init(accountSid, authToken);


            Message message = Message.creator(
                    new PhoneNumber("whatsapp:" + payload.getPhone()),
                    new PhoneNumber(twilioWhatsappNumber),
                    payload.getMessage()
            ).create();

            log.info("Direct delivery execution success. SID: {}", message.getSid());
            return ResponseEntity.ok("Message sent dynamically via background server framework!");
        } catch (Exception e) {
            log.error("Twilio cloud gateway failure: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Gateway delivery failed: " + e.getMessage());
        }
    }
}

