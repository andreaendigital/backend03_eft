package cl.bancoxyz.eft.clientes.service;

import cl.bancoxyz.eft.clientes.model.Cliente;
import cl.bancoxyz.eft.clientes.repository.ClienteRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {
    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) { this.repository = repository; }

    @Transactional
    public Cliente crear(String rut, String nombres, String apellidos, String email, String telefono) {
        return repository.save(new Cliente(rut, nombres, apellidos, email, telefono));
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar() { return repository.findAll(); }

    @Transactional(readOnly = true)
    public Cliente buscar(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + id));
    }
}
