package cl.bancoxyz.eft.batch.repository.cuentas;

import cl.bancoxyz.eft.batch.model.cuentas.EstadoFinanciero;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;

/**
 * Repositorio de estados financieros anuales.
 * Opera sobre banco_xyz_cuentas (cuentasEntityManagerFactory).
 */
public interface EstadoFinancieroRepository extends JpaRepository<EstadoFinanciero, Long> {
    boolean existsByCuentaIdAndFechaAndTransaccion(String cuentaId, LocalDate fecha, String transaccion);
}
