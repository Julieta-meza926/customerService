package com.mycompany.clientservice.service;

import com.mycompany.clientservice.command.CreateClientCommand;
import com.mycompany.clientservice.command.UpdateClientCommand;
import com.mycompany.clientservice.entity.Client;
import com.mycompany.clientservice.model.dto.ClientDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientCommandService {

    private final ClientService clientService;
    private final ClientEventService clientEventService;

    @Transactional
    public ClientDTO createClient(CreateClientCommand command) {
        log.info("Creando nuevo cliente: {}", command.getEmail());
        Client client = clientService.createClient(command);
        clientEventService.publishClientCreated(client);
        return mapToDTO(client);
    }

    @Transactional
    public ClientDTO updateClient(Long id, UpdateClientCommand command) {
        log.info("Actualizando cliente: {}", command.getEmail());
        Client client = clientService.updateClient(id, command);
        clientEventService.publishClientUpdated(client);
        return mapToDTO(client);
    }

    @Transactional
    public ClientDTO patchClient(Long id, Map<String, Object> updates) {
        Client client = clientService.patchClient(id, updates);
        clientEventService.publishClientUpdated(client);
        return mapToDTO(client);
    }

    @Transactional
    public void deleteClient(Long id) {
        log.info("Creando nuevo cliente con id :", id);
        Client client = clientService.deleteClient(id);
        clientEventService.publishClientDeleted(client);
    }

    private ClientDTO mapToDTO(Client client) {
        return ClientDTO.builder()
                .id(client.getId())
                .firstName(client.getFirstName())
                .lastName(client.getLastName())
                .email(client.getEmail())
                .build();
    }
}

