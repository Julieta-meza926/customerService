package com.mycompany.clientservice.service;

import com.mycompany.clientservice.command.CreateClientCommand;
import com.mycompany.clientservice.command.UpdateClientCommand;
import com.mycompany.clientservice.entity.Client;
import com.mycompany.clientservice.model.dto.ClientDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClientCommandService {

    private final ClientService clientService;
    private final ClientEventService clientEventService;

    public ClientDTO createClient(CreateClientCommand command) {
        Client client = clientService.createClient(command);
        clientEventService.publishClientCreated(client);
        return mapToDTO(client);
    }

    public ClientDTO updateClient(Long id, UpdateClientCommand command) {
        Client client = clientService.updateClient(id, command);
        clientEventService.publishClientUpdated(client);
        return mapToDTO(client);
    }

    public ClientDTO patchClient(Long id, Map<String, Object> updates) {
        Client client = clientService.patchClient(id, updates);
        clientEventService.publishClientUpdated(client);
        return mapToDTO(client);
    }

    public void deleteClient(Long id) {
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

