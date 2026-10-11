package cl.bancoxyz.eft.cuentas.repository;

import cl.bancoxyz.eft.cuentas.model.Cuenta;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuentaRepository extends JpaRepository<Cuenta, UUID> {
    List<Cuenta> findByClienteId(UUID clienteId);
}
