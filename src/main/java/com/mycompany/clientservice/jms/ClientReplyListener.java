package com.mycompany.clientservice.jms;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ClientReplyListener {

    @JmsListener(destination = "client.reply.queue")
    public void receiveReply(String message) {
        log.info("Respuesta recibida: {}", message);
    }
}
