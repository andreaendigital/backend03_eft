package cl.bancoxyz.eft.batch.repository.cuentas;

import cl.bancoxyz.eft.batch.model.cuentas.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de movimientos financieros diarios.
 * Opera sobre banco_xyz_cuentas (cuentasEntityManagerFactory).
 */
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    boolean existsBySourceId(String sourceId);
}
