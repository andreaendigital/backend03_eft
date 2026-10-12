package cl.bancoxyz.eft.batch.writer;

import cl.bancoxyz.eft.batch.model.cuentas.Movimiento;
import cl.bancoxyz.eft.batch.repository.cuentas.MovimientoRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

/**
 * Escribe movimientos financieros diarios en banco_xyz_cuentas.
 * Idempotente: omite registros cuyo source_id ya existe.
 */
@Component
public class IdempotentMovimientoWriter implements ItemWriter<Movimiento> {

    private final MovimientoRepository repository;

    public IdempotentMovimientoWriter(MovimientoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void write(Chunk<? extends Movimiento> chunk) {
        chunk.getItems().stream()
                .filter(item -> !repository.existsBySourceId(item.getSourceId()))
                .forEach(repository::save);
    }
}
