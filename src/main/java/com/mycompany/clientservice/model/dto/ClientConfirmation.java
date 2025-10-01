package com.mycompany.clientservice.model.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientConfirmation implements Serializable {
    private Long clientId;
    private String status; // Ej: "OK", "FAILED"
}

