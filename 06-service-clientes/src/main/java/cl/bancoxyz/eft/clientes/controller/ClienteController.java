package cl.bancoxyz.eft.clientes.controller;

import cl.bancoxyz.eft.clientes.model.Cliente;
import cl.bancoxyz.eft.clientes.service.ClienteService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {
    private final ClienteService service;

    public ClienteController(ClienteService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse crear(@RequestBody CrearCliente request) {
        return ClienteResponse.from(service.crear(request.rut(), request.nombres(), request.apellidos(),
                request.email(), request.telefono()));
    }

    @GetMapping
    public List<ClienteResponse> listar() {
        return service.listar().stream().map(ClienteResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ClienteResponse buscar(@PathVariable UUID id) {
        return ClienteResponse.from(service.buscar(id));
    }

    public record CrearCliente(String rut, String nombres, String apellidos,
                               String email, String telefono) { }

    public record ClienteResponse(UUID id, String rut, String nombres, String apellidos,
                                  String email, String telefono, java.time.LocalDateTime creadoEn) {
        static ClienteResponse from(Cliente cliente) {
            return new ClienteResponse(cliente.getId(), cliente.getRut(), cliente.getNombres(),
                    cliente.getApellidos(), cliente.getEmail(), cliente.getTelefono(),
                    cliente.getCreadoEn());
        }
    }
}
