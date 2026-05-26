package com.leadflow.leadflow_backend.model;

import lombok.Data;

@Data
public class DirectMessagePayload {

    private String phone;
    private String message;
}
