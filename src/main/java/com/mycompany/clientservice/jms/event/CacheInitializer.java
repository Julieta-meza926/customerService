package com.mycompany.clientservice.jms.event;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CacheInitializer {

    private final CacheManager cacheManager;
//Elimina cache
    @EventListener(ApplicationReadyEvent.class)
    public void clearCachesOnStartup() {
        cacheManager.getCache("clientCache").clear();
    }
}

