package com.mycompany.clientservice.jms;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.clientservice.model.dto.EventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomerOrderListener {

    private final ObjectMapper objectMapper;

    @JmsListener(destination = "order.queue")
    public void receiveOrder(EventDTO event) {
            log.info("recibi orden : {}", event);

    }
}

