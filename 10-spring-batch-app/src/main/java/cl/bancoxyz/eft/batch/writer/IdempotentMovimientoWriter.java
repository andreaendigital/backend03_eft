package cl.bancoxyz.eft.batch.writer;

import cl.bancoxyz.eft.batch.model.Movimiento;
import cl.bancoxyz.eft.batch.repository.MovimientoRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class IdempotentMovimientoWriter implements ItemWriter<Movimiento> {
    private final MovimientoRepository repository;
    public IdempotentMovimientoWriter(MovimientoRepository repository) { this.repository = repository; }
    @Override public void write(Chunk<? extends Movimiento> chunk) {
        chunk.getItems().stream().filter(item -> !repository.existsBySourceId(item.getSourceId()))
                .forEach(repository::save);
    }
}
