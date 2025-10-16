package com.mycompany.clientservice.service;

import com.mycompany.clientservice.jms.JmsMessageProducer;
import com.mycompany.clientservice.jms.event.ClientChangedEvent;
import com.mycompany.clientservice.entity.Client;
import com.mycompany.shared.dto.ClientEventDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientEventService {

    private final JmsMessageProducer jmsMessageProducer;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishClientCreatedEvent(Client client) {
        ClientEventDTO event = new ClientEventDTO("CLIENT_CREATED", client.getId(), client.getEmail(), "Cliente creado");
        jmsMessageProducer.sendClientEvent(event);
        log.info("Evento CLIENT_CREATED enviado después del commit para cliente {}", client.getId());
    }
    @Async
    @CircuitBreaker(name = "sendClientEventCircuitBreaker", fallbackMethod = "sendClientEventFallback")
    public void publishClientCreated(Client client) {
        sendClientEvent("CLIENT_CREATED", client, "Cliente creado");
    }

    @Async
    @CircuitBreaker(name = "sendClientEventCircuitBreaker", fallbackMethod = "sendClientEventFallback")
    public void publishClientUpdated(Client client) {
        sendClientEvent("CLIENT_UPDATED", client, "Cliente actualizado");
    }

    @Async
    @CircuitBreaker(name = "sendClientEventCircuitBreaker", fallbackMethod = "sendClientEventFallback")
    public void publishClientDeleted(Client client) {
        sendClientEvent("CLIENT_DELETED", client, "Cliente eliminado");
    }

    private void sendClientEvent(String eventType, Client client, String message) {
        jmsMessageProducer.sendClientEvent(
                new ClientEventDTO(eventType, client.getId(), client.getEmail(), message)
        );
        eventPublisher.publishEvent(new ClientChangedEvent(client.getId()));
        log.debug("Evento {} enviado para clientId={}", eventType, client.getId());
    }

    public void sendClientEventFallback( Client client, Throwable t) {
        log.error("CircuitBreaker activado: no se pudo enviar evento JMS para clientId={}", client.getId(), t);
        // Opcional: guardar evento en DB para reintento
    }

}

