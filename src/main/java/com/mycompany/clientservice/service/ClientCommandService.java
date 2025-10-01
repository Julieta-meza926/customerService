package com.mycompany.clientservice.service;
import com.mycompany.clientservice.command.CreateClientCommand;
import com.mycompany.clientservice.command.UpdateClientCommand;
import com.mycompany.clientservice.exception.ClientNotFoundException;
import com.mycompany.clientservice.exception.EmailAlreadyExistsException;
import com.mycompany.clientservice.jms.event.ClientChangedEvent;
import com.mycompany.clientservice.jms.JmsMessageProducer;
import com.mycompany.clientservice.entity.Client;
import com.mycompany.clientservice.model.dto.ClientDTO;
import com.mycompany.clientservice.repository.ClientRepository;
import com.mycompany.shared.dto.ClientEventDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientCommandService {

    private final ClientRepository clientRepository;
    private final JmsMessageProducer jmsMessageProducer;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ClientDTO createClient(CreateClientCommand command) {
        log.info("Creando nuevo cliente con email={}", command.getEmail());

        if (clientRepository.existsByEmail(command.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "El email ya está registrado: " + command.getEmail()
            );
        }

        Client client = new Client();
        client.setFirstName(command.getFirstName());
        client.setLastName(command.getLastName());
        client.setEmail(command.getEmail());

        Client savedClient = clientRepository.save(client);
        log.info("Cliente creado con id={}", savedClient.getId());

        publishClientChange("CLIENT_CREATED", savedClient, "New client created.");


        jmsMessageProducer.sendClientEvent(
                new ClientEventDTO("CLIENT_CREATED", client.getId(), client.getEmail(), "Cliente creado")
        );
        return mapToDTO(savedClient);
    }

    @Transactional
    public ClientDTO updateClient(Long id, @Valid UpdateClientCommand command) {
        log.info("Actualizando cliente id={}", id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("Cliente no encontrado con id=" + id));

        String newEmail = command.getEmail();
        if (!newEmail.equals(client.getEmail()) && clientRepository.existsByEmail(newEmail)) {
            throw new EmailAlreadyExistsException("El email ya está registrado: " + newEmail);
        }
        client.setFirstName(command.getFirstName());
        client.setLastName(command.getLastName());
        client.setEmail(newEmail);

        Client updatedClient = clientRepository.save(client);
        log.info("Cliente actualizado id={}", updatedClient.getId());

        publishClientChange("CLIENT_UPDATED", updatedClient, "Client fully updated.");

        return mapToDTO(updatedClient);
    }


    @Transactional
    public ClientDTO patchClient(Long id, Map<String, Object> updates) {
        log.info("Parcheando cliente id={} con campos={}", id, updates.keySet());

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("Cliente no encontrado con id=" + id));

        // Mapa de actualizaciones soportadas
        Map<String, Consumer<Object>> updaters = Map.of(
                "firstName", value -> client.setFirstName((String) value),
                "lastName", value -> client.setLastName((String) value),
                "email", value -> client.setEmail((String) value)
        );

        // Aplicamos solo los campos válidos
        updates.forEach((key, value) -> {
            Consumer<Object> updater = updaters.get(key);
            if (updater != null && value instanceof String) {
                updater.accept(value);
                log.debug("Campo '{}' actualizado con valor '{}'", key, value);
            } else {
                log.warn("Campo '{}' no es soportado o valor inválido: {}", key, value);
            }
        });

        Client patchedClient = clientRepository.save(client);
        log.info("Cliente parcialmente actualizado id={}", patchedClient.getId());

        publishClientChange("CLIENT_UPDATED", patchedClient, "Client partially updated.");

        return mapToDTO(patchedClient);
    }

    @Transactional
    public void deleteClient(Long id) {
        log.info("Intentando eliminar cliente id={}", id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("Cliente no encontrado con id=" + id));

        clientRepository.delete(client);
        log.info("Cliente eliminado id={}", id);

        try {
            sendClientEventAsync("CLIENT_DELETED", client, "Client deleted.");
        } catch (Exception e) {
            log.warn("No se pudo enviar evento JMS: {}", e.getMessage());
        }
    }

    @Async
    @CircuitBreaker(name = "sendClientEventCircuitBreaker", fallbackMethod = "sendClientEventFallback")
    public void sendClientEventAsync(String eventType, Client client, String message) {
        jmsMessageProducer.sendClientEvent(
                new ClientEventDTO(eventType, client.getId(), client.getEmail(), message)
        );
        log.debug("Evento JMS {} enviado para clientId={}", eventType, client.getId());
    }

    public void sendClientEventFallback(String eventType, Client client, String message, Throwable t) {
        log.error("CircuitBreaker activado: no se pudo enviar evento JMS para clientId={}", client.getId(), t);
        // Opcional: guardar evento en DB para reintento
    }

    private ClientDTO mapToDTO(Client client) {
        return ClientDTO.builder()
                .id(client.getId())
                .firstName(client.getFirstName())
                .lastName(client.getLastName())
                .email(client.getEmail())
                .build();
    }
    private void publishClientChange(String eventType, Client client, String message) {
        sendClientEventAsync(eventType, client, message);
        eventPublisher.publishEvent(new ClientChangedEvent(client.getId()));
    }
}

