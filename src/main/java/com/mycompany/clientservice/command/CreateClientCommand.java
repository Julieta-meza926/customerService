package com.mycompany.clientservice.command;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateClientCommand {

    @NotBlank (message = "El nombre es obligatorio")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    private String lastName;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Agrega un Email valido para continuar")
    private String email;


    public CreateClientCommand(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;

    }
}
