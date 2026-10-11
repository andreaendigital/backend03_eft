package cl.bancoxyz.eft.pagos.repository;

import cl.bancoxyz.eft.pagos.model.Pago;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, UUID> { }
