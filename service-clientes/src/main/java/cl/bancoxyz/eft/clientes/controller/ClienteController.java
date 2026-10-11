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
    public Cliente crear(@RequestBody CrearCliente request) {
        return service.crear(request.rut(), request.nombres(), request.apellidos(),
                request.email(), request.telefono());
    }

    @GetMapping
    public List<Cliente> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public Cliente buscar(@PathVariable UUID id) { return service.buscar(id); }

    public record CrearCliente(String rut, String nombres, String apellidos,
                               String email, String telefono) { }
}
