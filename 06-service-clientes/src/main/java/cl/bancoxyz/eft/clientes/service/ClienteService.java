package cl.bancoxyz.eft.clientes.service;

import cl.bancoxyz.eft.clientes.model.Cliente;
import cl.bancoxyz.eft.clientes.messaging.KafkaEventPublisher;
import cl.bancoxyz.eft.clientes.error.ResourceNotFoundException;
import cl.bancoxyz.eft.clientes.repository.ClienteRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {
    private final ClienteRepository repository;
    private final KafkaEventPublisher events;

    public ClienteService(ClienteRepository repository, KafkaEventPublisher events) {
        this.repository = repository;
        this.events = events;
    }

    @Transactional
    public Cliente crear(String rut, String nombres, String apellidos, String email, String telefono) {
        Cliente cliente = repository.save(new Cliente(rut, nombres, apellidos, email, telefono));
        events.accountChanged(cliente);
        return cliente;
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar() { return repository.findAll(); }

    @Transactional(readOnly = true)
    public Cliente buscar(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id));
    }
}
