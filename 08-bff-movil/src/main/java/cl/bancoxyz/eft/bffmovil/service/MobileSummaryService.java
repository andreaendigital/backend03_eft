package cl.bancoxyz.eft.bffmovil.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class MobileSummaryService {
    private final WebClient client;
    private final String cuentasUrl;

    public MobileSummaryService(WebClient client,
                                @Value("${services.cuentas-url:lb://SERVICE-CUENTAS}") String cuentasUrl) {
        this.client = client;
        this.cuentasUrl = cuentasUrl;
    }

    @CircuitBreaker(name = "cuentas", fallbackMethod = "fallback")
    public Mono<List<Map<String, Object>>> resumen(UUID clienteId) {
        return client.get().uri(cuentasUrl + "/api/v1/cuentas/cliente/{id}", clienteId)
                .retrieve().bodyToFlux(Map.class)
                .map(account -> Map.of("id", account.get("id"),
                        "numero", account.get("numero"),
                        "saldo", account.get("saldo")))
                .collectList();
    }

    public Mono<List<Map<String, Object>>> fallback(UUID clienteId, Throwable cause) {
        return Mono.just(List.of());
    }
}
