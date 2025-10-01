package com.mycompany.clientservice.model.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class RegisterRequest implements Serializable {

    @NotBlank (message = "username is required")
    private String username;

    @NotBlank (message = "password is required")
    private String password;

    @NotBlank(message = "El rol es obligatorio")
    private String role;
}

