package com.mycompany.clientservice.repository;
import com.mycompany.clientservice.entity.Client;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    boolean existsByEmail(@NotBlank(message = "El email es obligatorio") @Email(message = "Agrega un Email valido para continuar") String email);
}
