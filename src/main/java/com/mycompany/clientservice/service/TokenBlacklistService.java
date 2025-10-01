package com.mycompany.clientservice.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    //herramienta para leer y escribir en redis
    private final RedisTemplate<String, String> redisTemplate;

    public void revokeToken(String token, long expirationMillis) {
        String key = "blacklist:" + token;
        redisTemplate.opsForValue().set(key, "revoked", expirationMillis, TimeUnit.MILLISECONDS);
        log.info("Token revocado y guardado en blacklist: {}", token);
    }

    public boolean isTokenRevoked(String token) {
        String key = "blacklist:" + token;
        return redisTemplate.hasKey(key);
    }
}
