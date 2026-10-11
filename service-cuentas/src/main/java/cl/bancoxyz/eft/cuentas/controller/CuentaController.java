package cl.bancoxyz.eft.cuentas.controller;

import cl.bancoxyz.eft.cuentas.model.Cuenta;
import cl.bancoxyz.eft.cuentas.model.TipoCuenta;
import cl.bancoxyz.eft.cuentas.service.CuentaService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cuentas")
public class CuentaController {
    private final CuentaService service;

    public CuentaController(CuentaService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Cuenta abrir(@RequestBody AperturaCuenta request) {
        return service.abrir(request.clienteId(), request.numero(), request.tipo());
    }

    @PostMapping("/{id}/cierre")
    public Cuenta cerrar(@PathVariable UUID id) { return service.cerrar(id); }

    @GetMapping("/cliente/{clienteId}")
    public List<Cuenta> listar(@PathVariable UUID clienteId) {
        return service.listarPorCliente(clienteId);
    }

    public record AperturaCuenta(UUID clienteId, String numero, TipoCuenta tipo) { }
}
