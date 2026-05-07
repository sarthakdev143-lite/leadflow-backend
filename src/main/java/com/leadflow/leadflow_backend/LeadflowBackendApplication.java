package com.leadflow.leadflow_backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class LeadflowBackendApplication {

    public static void main(final String[] args) {
        Dotenv dotenv = Dotenv.load();

        System.setProperty("spring.data.mongodb.uri", dotenv.get("MONGO_URI"));
        SpringApplication.run(LeadflowBackendApplication.class, args);
        System.out.println("Application started");
    }

}