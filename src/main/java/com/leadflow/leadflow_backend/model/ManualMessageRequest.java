package com.leadflow.leadflow_backend.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManualMessageRequest {


    private String chatId;
    private String message;
}
