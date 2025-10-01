package com.mycompany.clientservice.service;
import com.mycompany.clientservice.exception.ClientNotFoundException;
import com.mycompany.clientservice.entity.Client;
import com.mycompany.clientservice.model.dto.ClientDTO;
import com.mycompany.clientservice.repository.ClientRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientQueryService {

    private final ClientRepository clientRepository;

    @Cacheable(value = "clients", key = "#clientId")
    @CircuitBreaker(name = "clientServiceCircuitBreaker", fallbackMethod = "getClientFallback")
    public ClientDTO getClient(Long clientId) {
        log.info(">>> Consultando cliente id={} en DB (si no está en cache)", clientId);
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException("Cliente no encontrado con id=" + clientId));
        return mapToDTO(client);
    }

    @Cacheable(value = "clients", key = "'all'")
    @CircuitBreaker(name = "clientServiceCircuitBreaker", fallbackMethod = "getAllClientsFallback")
    public List<ClientDTO> getAllClients() {
        log.info(">>> Consultando todos los clientes en DB (si no está en cache)");
        return clientRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @CacheEvict(value = "clients", allEntries = true)
    public void evictAllClientsCache() {
        log.info(">>> Cache de clientes eliminada manualmente");
    }

    public ClientDTO getClientFallback(Long clientId, Throwable t) {
        log.warn("Fallback getClient para id={}", clientId, t);
        return ClientDTO.builder()
                .id(clientId)
                .firstName("Desconocido")
                .lastName("Desconocido")
                .email("noemail@fallback.com")
                .build();
    }

    public List<ClientDTO> getAllClientsFallback(Throwable t) {
        log.warn("Fallback getAllClients", t);
        return List.of(
                ClientDTO.builder()
                        .id(-1L)
                        .firstName("Servicio")
                        .lastName("No disponible")
                        .email("fallback@service.com")
                        .build()
        );
    }

    private ClientDTO mapToDTO(Client client) {
        return ClientDTO.builder()
                .id(client.getId())
                .firstName(client.getFirstName())
                .lastName(client.getLastName())
                .email(client.getEmail())
                .build();
    }
}
