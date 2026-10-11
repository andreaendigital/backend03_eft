package cl.bancoxyz.eft.bffcajeros.controller;

import cl.bancoxyz.eft.bffcajeros.service.AtmOperationsService;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/atm")
public class AtmController {
    private final AtmOperationsService operations;

    public AtmController(AtmOperationsService operations) { this.operations = operations; }

    @GetMapping("/cuentas/{cuentaId}/saldo")
    public Mono<Map<String, Object>> saldo(@PathVariable UUID cuentaId) {
        return operations.saldo(cuentaId);
    }

    @PostMapping("/cuentas/{cuentaId}/retiros")
    public Mono<Map<String, Object>> retiro(@PathVariable UUID cuentaId,
                                             @RequestBody MontoRequest request) {
        return operations.retiro(cuentaId, request.monto());
    }

    @PostMapping("/cuentas/{cuentaId}/depositos")
    public Mono<Map<String, Object>> deposito(@PathVariable UUID cuentaId,
                                               @RequestBody MontoRequest request) {
        return operations.deposito(cuentaId, request.monto());
    }

    public record MontoRequest(BigDecimal monto) { }
}
