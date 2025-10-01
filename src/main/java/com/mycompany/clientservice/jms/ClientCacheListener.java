package com.mycompany.clientservice.jms;
import com.mycompany.clientservice.jms.event.ClientChangedEvent;
import com.mycompany.clientservice.service.ClientQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientCacheListener {

    private final ClientQueryService clientQueryService;

    @EventListener
    public void handleClientChanged (ClientChangedEvent event){
        clientQueryService.evictAllClientsCache();
        System.out.println("cache de clientes eliminado para evento :" + event.getClientId());
    }
}
