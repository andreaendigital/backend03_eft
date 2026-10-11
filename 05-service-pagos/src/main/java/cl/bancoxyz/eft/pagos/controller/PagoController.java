package cl.bancoxyz.eft.pagos.controller;

import cl.bancoxyz.eft.pagos.model.Pago;
import cl.bancoxyz.eft.pagos.model.TipoPago;
import cl.bancoxyz.eft.pagos.service.PagoWorkflowService;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pagos")
public class PagoController {
    private final PagoWorkflowService service;

    public PagoController(PagoWorkflowService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pago crear(@RequestBody CrearPago request) {
        return service.process(request.cuentaOrigenId(), request.cuentaDestinoId(),
                request.monto(), request.tipo());
    }

    public record CrearPago(UUID cuentaOrigenId, UUID cuentaDestinoId,
                            BigDecimal monto, TipoPago tipo) { }
}
