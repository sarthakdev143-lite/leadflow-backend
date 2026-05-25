package com.leadflow.leadflow_backend.rest;
import com.leadflow.leadflow_backend.model.SendResponse;
import com.leadflow.leadflow_backend.model.TelegramRequest;
import com.leadflow.leadflow_backend.teleException.TelegramException;
import com.leadflow.leadflow_backend.service.TelegramService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/telegram")
public class TelegramController {

    private static final Logger logger = LoggerFactory.getLogger(TelegramController.class);

    @Autowired
    private TelegramService telegramService;


    @PostMapping("/send")
    public ResponseEntity<SendResponse> sendTelegramMessage(
            @Valid @RequestBody TelegramRequest request) {
        System.out.println("\n\nSending Telegram message!!!");

        logger.info("Received Telegram send request for lead: {}", request.getName());

        try {

            SendResponse response = telegramService.sendMessage(
                    request.getName(),
                    request.getPhone(),
                    request.getSource(),
                    request.getType(),
                    request.getMessage(),
                    request.getLeadChatId()
            );
            return ResponseEntity.ok(response);

        } catch (TelegramException e) {
            logger.error("TelegramException: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new SendResponse(false, "Failed to send Telegram message"));

        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SendResponse(false, "Internal server error"));
        }
    }
}