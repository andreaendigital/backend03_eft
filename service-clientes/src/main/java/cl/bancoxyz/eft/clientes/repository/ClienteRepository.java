package cl.bancoxyz.eft.clientes.repository;

import cl.bancoxyz.eft.clientes.model.Cliente;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    Optional<Cliente> findByRut(String rut);
}
