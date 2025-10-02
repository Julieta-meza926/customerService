package com.mycompany.clientservice.jms;
import com.mycompany.clientservice.model.dto.ClientEventDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JmsMessageProducer {

    private final JmsTemplate jmsTemplate;
    private static final String CLIENT_QUEUE = "client.queue";

    @CircuitBreaker(name = "clientServiceBreaker", fallbackMethod = "sendFallback")
    public void sendClientEvent(ClientEventDTO event) {
        System.out.println("Sending client event: " + event.getEventType() +
                " for client ID: " + event.getClientId());
        jmsTemplate.convertAndSend(CLIENT_QUEUE, event);
    }

    private void sendFallback (ClientEventDTO event, Throwable t){
        log.error ("no se pudo enviar el evento {}. Error: {}" , event, t.getMessage());
    }
}
