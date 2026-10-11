package cl.bancoxyz.eft.batch.repository;
import cl.bancoxyz.eft.batch.model.InteresTrimestral;
import org.springframework.data.jpa.repository.JpaRepository;
public interface InteresRepository extends JpaRepository<InteresTrimestral, Long> {
    boolean existsByCuentaId(String cuentaId);
}
