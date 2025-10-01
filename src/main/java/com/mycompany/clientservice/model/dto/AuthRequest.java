package com.mycompany.clientservice.model.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.io.Serializable;

@Data
public class AuthRequest implements Serializable {
    @NotBlank (message = "username is required")
    private String username;
    @NotBlank(message = "password is required")
    private String password;
}
