package com.mycompany.clientservice.service;

import com.mycompany.clientservice.command.CreateClientCommand;
import com.mycompany.clientservice.command.UpdateClientCommand;
import com.mycompany.clientservice.entity.Client;
import com.mycompany.clientservice.exception.ClientNotFoundException;
import com.mycompany.clientservice.exception.EmailAlreadyExistsException;
import com.mycompany.clientservice.repository.ClientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    @Transactional
    public Client createClient(CreateClientCommand command) {
        if (clientRepository.existsByEmail(command.getEmail())) {
            throw new EmailAlreadyExistsException("El email ya está registrado: " + command.getEmail());
        }

        Client client = new Client();
        client.setFirstName(command.getFirstName());
        client.setLastName(command.getLastName());
        client.setEmail(command.getEmail());

        return clientRepository.save(client);
    }

    @Transactional
    public Client updateClient(Long id, UpdateClientCommand command) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("Cliente no encontrado con id=" + id));

        String newEmail = command.getEmail();
        if (!newEmail.equals(client.getEmail()) && clientRepository.existsByEmail(newEmail)) {
            throw new EmailAlreadyExistsException("El email ya está registrado: " + newEmail);
        }

        client.setFirstName(command.getFirstName());
        client.setLastName(command.getLastName());
        client.setEmail(newEmail);

        return clientRepository.save(client);
    }

    @Transactional
    public Client patchClient(Long id, Map<String, Object> updates) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("Cliente no encontrado con id=" + id));

        Map<String, Consumer<Object>> updaters = Map.of(
                "firstName", value -> client.setFirstName((String) value),
                "lastName", value -> client.setLastName((String) value),
                "email", value -> client.setEmail((String) value)
        );

        updates.forEach((key, value) -> {
            Consumer<Object> updater = updaters.get(key);
            if (updater != null && value instanceof String) {
                updater.accept(value);
            }
        });

        return clientRepository.save(client);
    }

    @Transactional
    public Client deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("Cliente no encontrado con id=" + id));

        clientRepository.delete(client);
        return client;
    }
}



