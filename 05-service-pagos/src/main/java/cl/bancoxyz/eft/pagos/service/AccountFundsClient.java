package cl.bancoxyz.eft.pagos.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AccountFundsClient {
    private final RestClient client;

    public AccountFundsClient(RestClient client) {
        this.client = client;
    }

    @CircuitBreaker(name = "cuentas")
    @TimeLimiter(name = "cuentas")
    @Retry(name = "cuentas")
    public CompletableFuture<Boolean> reserve(UUID accountId, BigDecimal amount, UUID paymentId) {
        return CompletableFuture.supplyAsync(() -> Boolean.TRUE.equals(client.post()
                .uri("/api/v1/cuentas/{id}/reservas", accountId)
                .header("Idempotency-Key", paymentId.toString())
                .body(new FundsRequest(amount))
                .retrieve()
                .body(Boolean.class)));
    }

    @CircuitBreaker(name = "cuentas")
    @TimeLimiter(name = "cuentas")
    @Retry(name = "cuentas")
    public CompletableFuture<Void> confirm(UUID accountId, UUID paymentId) {
        return CompletableFuture.runAsync(() -> client.post()
                .uri("/api/v1/cuentas/{id}/reservas/{paymentId}/confirmar", accountId, paymentId)
                .retrieve()
                .toBodilessEntity());
    }

    @CircuitBreaker(name = "cuentas")
    @TimeLimiter(name = "cuentas")
    @Retry(name = "cuentas")
    public CompletableFuture<Void> release(UUID accountId, UUID paymentId) {
        return CompletableFuture.runAsync(() -> client.post()
                .uri("/api/v1/cuentas/{id}/reservas/{paymentId}/liberar", accountId, paymentId)
                .retrieve()
                .toBodilessEntity());
    }

    private record FundsRequest(BigDecimal amount) { }
}
