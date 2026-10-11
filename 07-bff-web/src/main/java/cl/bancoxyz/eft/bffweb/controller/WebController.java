package cl.bancoxyz.eft.bffweb.controller;

import cl.bancoxyz.eft.bffweb.service.WebAggregationService;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/web")
public class WebController {
    private final WebAggregationService aggregationService;

    public WebController(WebAggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    @GetMapping("/clientes/{clienteId}/dashboard")
    public Mono<Map<String, Object>> dashboard(@PathVariable UUID clienteId) {
        return Mono.zip(aggregationService.cliente(clienteId), aggregationService.cuentas(clienteId))
                .map(result -> Map.of("cliente", result.getT1(), "cuentas", result.getT2()));
    }
}
