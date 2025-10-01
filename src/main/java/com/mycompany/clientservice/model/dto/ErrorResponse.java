package com.mycompany.clientservice.model.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse implements Serializable {
    private int status;
    private String error;
    private String path;
    private LocalDateTime timestamp;
}

