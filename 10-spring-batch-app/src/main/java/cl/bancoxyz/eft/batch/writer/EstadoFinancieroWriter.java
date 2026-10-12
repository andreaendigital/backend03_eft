package cl.bancoxyz.eft.batch.writer;

import cl.bancoxyz.eft.batch.model.cuentas.EstadoFinanciero;
import cl.bancoxyz.eft.batch.repository.cuentas.EstadoFinancieroRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

/**
 * Escribe estados financieros anuales en banco_xyz_cuentas.
 * Idempotente: omite registros con la misma clave (cuentaId, fecha, transaccion).
 */
@Component
public class EstadoFinancieroWriter implements ItemWriter<EstadoFinanciero> {

    private final EstadoFinancieroRepository repository;

    public EstadoFinancieroWriter(EstadoFinancieroRepository repository) {
        this.repository = repository;
    }

    @Override
    public void write(Chunk<? extends EstadoFinanciero> chunk) {
        chunk.getItems().stream()
                .filter(item -> !repository.existsByCuentaIdAndFechaAndTransaccion(
                        item.getCuentaId(), item.getFecha(), item.getTransaccion()))
                .forEach(repository::save);
    }
}
