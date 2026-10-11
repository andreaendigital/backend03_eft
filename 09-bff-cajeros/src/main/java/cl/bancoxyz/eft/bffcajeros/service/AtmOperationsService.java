package cl.bancoxyz.eft.bffcajeros.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AtmOperationsService {
    private final WebClient client;
    private final String cuentasUrl;
    private final String pagosUrl;

    public AtmOperationsService(WebClient client,
                                @Value("${services.cuentas-url:lb://SERVICE-CUENTAS}") String cuentasUrl,
                                @Value("${services.pagos-url:lb://SERVICE-PAGOS}") String pagosUrl) {
        this.client = client;
        this.cuentasUrl = cuentasUrl;
        this.pagosUrl = pagosUrl;
    }

    @CircuitBreaker(name = "cuentas", fallbackMethod = "saldoFallback")
    public Mono<Map<String, Object>> saldo(UUID cuentaId) {
        return client.get().uri(cuentasUrl + "/api/v1/cuentas/{id}", cuentaId)
                .retrieve().bodyToMono(Map.class).map(value -> (Map<String, Object>) value);
    }

    @CircuitBreaker(name = "pagos", fallbackMethod = "operacionFallback")
    public Mono<Map<String, Object>> retiro(UUID cuentaId, BigDecimal monto) {
        return operar(cuentaId, monto, "TRANSFERENCIA");
    }

    @CircuitBreaker(name = "pagos", fallbackMethod = "operacionFallback")
    public Mono<Map<String, Object>> deposito(UUID cuentaId, BigDecimal monto) {
        return operar(cuentaId, monto, "DEPOSITO");
    }

    private Mono<Map<String, Object>> operar(UUID cuentaId, BigDecimal monto, String tipo) {
        Map<String, Object> request = Map.of("cuentaOrigenId", cuentaId,
                "monto", monto, "tipo", tipo);
        return client.post().uri(pagosUrl + "/api/v1/pagos")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(request)
                .retrieve().bodyToMono(Map.class).map(value -> (Map<String, Object>) value);
    }

    public Mono<Map<String, Object>> saldoFallback(UUID cuentaId, Throwable cause) {
        return Mono.just(Map.of("cuentaId", cuentaId, "disponible", false));
    }

    public Mono<Map<String, Object>> operacionFallback(UUID cuentaId, BigDecimal monto, Throwable cause) {
        return Mono.just(Map.of("cuentaId", cuentaId, "monto", monto,
                "estado", "NO_DISPONIBLE"));
    }
}
