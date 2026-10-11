package cl.bancoxyz.eft.bffweb.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class WebAggregationService {
    private final WebClient client;
    private final String cuentasUrl;
    private final String clientesUrl;

    public WebAggregationService(WebClient client,
                                 @Value("${services.cuentas-url:lb://SERVICE-CUENTAS}") String cuentasUrl,
                                 @Value("${services.clientes-url:lb://SERVICE-CLIENTES}") String clientesUrl) {
        this.client = client;
        this.cuentasUrl = cuentasUrl;
        this.clientesUrl = clientesUrl;
    }

    @CircuitBreaker(name = "cuentas", fallbackMethod = "cuentasFallback")
    public Mono<List<Map<String, Object>>> cuentas(UUID clienteId) {
        return client.get().uri(cuentasUrl + "/api/v1/cuentas/cliente/{id}", clienteId)
                .retrieve().bodyToFlux(Map.class).map(value -> (Map<String, Object>) value).collectList();
    }

    @CircuitBreaker(name = "clientes", fallbackMethod = "clienteFallback")
    public Mono<Map<String, Object>> cliente(UUID clienteId) {
        return client.get().uri(clientesUrl + "/api/v1/clientes/{id}", clienteId)
                .retrieve().bodyToMono(Map.class).map(value -> (Map<String, Object>) value);
    }

    public Mono<List<Map<String, Object>>> cuentasFallback(UUID clienteId, Throwable cause) {
        return Mono.just(List.of());
    }

    public Mono<Map<String, Object>> clienteFallback(UUID clienteId, Throwable cause) {
        return Mono.just(Map.of("id", clienteId, "disponible", false));
    }
}
