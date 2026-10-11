package cl.bancoxyz.eft.batch.repository;
import cl.bancoxyz.eft.batch.model.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    boolean existsBySourceId(String sourceId);
}
