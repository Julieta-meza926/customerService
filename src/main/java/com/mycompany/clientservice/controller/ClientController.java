package com.mycompany.clientservice.controller;

import com.mycompany.clientservice.command.CreateClientCommand;
import com.mycompany.clientservice.command.UpdateClientCommand;
import com.mycompany.clientservice.model.dto.ClientDTO;
import com.mycompany.clientservice.service.ClientQueryService;
import com.mycompany.clientservice.service.ClientCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class ClientController {

    private final ClientCommandService commandHandler;
    private final ClientQueryService queryHandler;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientDTO> createClient(@RequestBody @Valid CreateClientCommand command) {
        ClientDTO clientDTO = commandHandler.createClient(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(clientDTO);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ClientDTO> getClient(@PathVariable Long id) {
        ClientDTO clientDTO = queryHandler.getClient(id);
        return ResponseEntity.ok(clientDTO);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<ClientDTO>> getAllClients() {
        List<ClientDTO> clients = queryHandler.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientDTO> updateClient(
            @PathVariable Long id,
            @RequestBody @Valid UpdateClientCommand command) {
        ClientDTO clientDTO = commandHandler.updateClient(id, command);
        return ResponseEntity.ok(clientDTO);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientDTO> patchClient(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        ClientDTO clientDTO = commandHandler.patchClient(id, updates);
        return ResponseEntity.ok(clientDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        commandHandler.deleteClient(id);
        return ResponseEntity.noContent().build();
    }
}
