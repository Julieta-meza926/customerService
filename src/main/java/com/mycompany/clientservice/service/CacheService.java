package com.mycompany.clientservice.service;
import com.mycompany.clientservice.exception.CacheException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.RequiredArgsConstructor;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
@Slf4j
@RequiredArgsConstructor
public class CacheService {

    private final CacheManager cacheManager;
    private final CircuitBreaker redisCircuitBreaker;


    private <T> T executeCache(String cacheName, Supplier<T> action) {
        return Try.ofSupplier(io.github.resilience4j.circuitbreaker.CircuitBreaker.decorateSupplier(redisCircuitBreaker, action))
                .recover(CallNotPermittedException.class, ex -> {
                    log.warn("CircuitBreaker abierto cache={}", cacheName, ex);
                    throw new CacheException("Servicio de cache no disponible (CircuitBreaker abierto)", ex);
                })
                .recover(Exception.class, ex -> {
                    log.warn("Fallo en Redis cache={}", cacheName, ex);
                    throw new CacheException("Error accediendo al cache", ex);
                })
                .get();
    }

    //Obtener valor
    public <T> T get(String cacheName, Object key, Supplier<T> fallback) {
        return executeCache(cacheName, () -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null && cache.get(key) != null) {
                return (T) cache.get(key).get();
            }
            return fallback.get();
        });
    }

    //Guardar valor
    public void put(String cacheName, Object key, Object value) {
        executeCache(cacheName, () -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) cache.put(key, value);
            return null;
        });
    }

    //Eliminar clave
    public void evict(String cacheName, Object key) {
        executeCache(cacheName, () -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) cache.evict(key);
            return null;
        });
    }

    //Limpiar todo
    public void evictAll(String cacheName) {
        executeCache(cacheName, () -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) cache.clear();
            return null;
        });
    }
}
