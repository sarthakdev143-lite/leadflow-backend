package com.leadflow.leadflow_backend.rest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class HealthCheckController {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckController.class);

    @Value("${server.port:8080}")
    private String serverPort;

    @GetMapping("/api/health")
    public String healthCheck() {
        log.info(">>>> Request successfully handled by backend instance running on port: {} <<<<", serverPort);
        return "Backend is running fine on port: " + serverPort;
    }
}