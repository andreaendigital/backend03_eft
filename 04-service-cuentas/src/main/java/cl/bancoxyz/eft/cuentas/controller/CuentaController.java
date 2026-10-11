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
    public CuentaResponse abrir(@RequestBody AperturaCuenta request) {
        return CuentaResponse.from(service.abrir(request.clienteId(), request.numero(), request.tipo()));
    }

    @PostMapping("/{id}/cierre")
    public CuentaResponse cerrar(@PathVariable UUID id) { return CuentaResponse.from(service.cerrar(id)); }

    @GetMapping("/cliente/{clienteId}")
    public List<CuentaResponse> listar(@PathVariable UUID clienteId) {
        return service.listarPorCliente(clienteId).stream().map(CuentaResponse::from).toList();
    }

    public record AperturaCuenta(UUID clienteId, String numero, TipoCuenta tipo) { }

    public record CuentaResponse(UUID id, UUID clienteId, String numero, TipoCuenta tipo,
                                 java.math.BigDecimal saldo, cl.bancoxyz.eft.cuentas.model.EstadoCuenta estado,
                                 java.time.LocalDateTime creadaEn, java.time.LocalDateTime cerradaEn) {
        static CuentaResponse from(Cuenta cuenta) {
            return new CuentaResponse(cuenta.getId(), cuenta.getClienteId(), cuenta.getNumero(),
                    cuenta.getTipo(), cuenta.getSaldo(), cuenta.getEstado(),
                    cuenta.getCreadaEn(), cuenta.getCerradaEn());
        }
    }
}
