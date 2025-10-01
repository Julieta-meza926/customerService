package com.mycompany.clientservice.config;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    //Configuración de conexión a Redis
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
      //es una fábrica de conexiones que Spring usa para comunicarse con Redis.
        return new LettuceConnectionFactory();
    }

    //Configuración de serialización y TTL de los caches
    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        // ObjectMapper para serializar cualquier tipo de objeto
        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(mapper);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // Tiempo de vida de cache
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                )
                .disableCachingNullValues(); // No cachea valores nulos
    }

    // Manager de cache que usa Redis
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfiguration())
                .build();
    }

    //CircuitBreaker para proteger llamadas a Redis
    @Bean
    public CircuitBreaker redisCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // % de fallos para abrir el breaker
                .waitDurationInOpenState(Duration.ofSeconds(10)) // tiempo en estado abierto
                .slidingWindowSize(10) // número de llamadas para calcular fallos
                .build();

        return CircuitBreaker.of("redisCircuitBreaker", config);
    }
}
