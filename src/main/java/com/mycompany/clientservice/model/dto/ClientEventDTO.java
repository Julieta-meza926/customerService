package com.mycompany.clientservice.model.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ClientEventDTO implements Serializable {
    private String eventType;
    private Long clientId;
    private String clientEmail;
    private String message;
}