package com.leadflow.leadflow_backend.rest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.InetAddress;

@RestController
public class HealthCheckController {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckController.class);

    @Value("${server.port:8080}")
    private String serverPort;
    
    @Value("${instance.id:unknown}")
    private String instanceId;

    @GetMapping("/api/health")
    public String healthCheck() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            log.info(">>>> Instance {} running on port {} (host: {}) <<<<", instanceId, serverPort, hostname);
            return String.format("OK - Instance: %s, Port: %s, Host: %s", instanceId, serverPort, hostname);
        } catch (Exception e) {
            return "OK - Port: " + serverPort;
        }
    }
}