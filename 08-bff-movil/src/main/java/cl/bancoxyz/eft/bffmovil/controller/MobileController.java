package cl.bancoxyz.eft.bffmovil.controller;

import cl.bancoxyz.eft.bffmovil.service.MobileSummaryService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/mobile")
public class MobileController {
    private final MobileSummaryService summaryService;

    public MobileController(MobileSummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @GetMapping("/clientes/{clienteId}/resumen")
    public Mono<Map<String, Object>> resumen(@PathVariable UUID clienteId) {
        return summaryService.resumen(clienteId).map(cuentas -> Map.of("cuentas", cuentas));
    }
}
