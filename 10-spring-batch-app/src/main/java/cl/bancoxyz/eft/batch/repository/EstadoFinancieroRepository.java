package cl.bancoxyz.eft.batch.repository;
import cl.bancoxyz.eft.batch.model.EstadoFinanciero;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
public interface EstadoFinancieroRepository extends JpaRepository<EstadoFinanciero, Long> {
    boolean existsByCuentaIdAndFechaAndTransaccion(String cuentaId, LocalDate fecha, String transaccion);
}
