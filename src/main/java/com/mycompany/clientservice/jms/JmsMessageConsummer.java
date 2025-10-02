package com.mycompany.clientservice.jms;
import com.mycompany.clientservice.model.dto.ClientEventDTO;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class JmsMessageConsummer {

    private static final String CLIENT_QUEUE = "client.queue";

    @JmsListener(destination = CLIENT_QUEUE)
    public void receiveClientEvent(ClientEventDTO event){
        System.out.println("Received client event: " + event.getEventType() + " for client ID: " + event.getClientId() + ". Message: " + event.getMessage());
    }
}
