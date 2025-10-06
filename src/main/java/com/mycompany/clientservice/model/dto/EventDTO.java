package com.mycompany.clientservice.model.dto;
import lombok.Data;
import java.io.Serializable;

@Data
public class EventDTO implements Serializable {
    private Long id;
    private Long customerId;
    private String product;
    private int quantity;
    private double total;
}

