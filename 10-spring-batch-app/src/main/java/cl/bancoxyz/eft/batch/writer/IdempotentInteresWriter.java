package cl.bancoxyz.eft.batch.writer;

import cl.bancoxyz.eft.batch.model.InteresTrimestral;
import cl.bancoxyz.eft.batch.repository.InteresRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class IdempotentInteresWriter implements ItemWriter<InteresTrimestral> {
    private final InteresRepository repository;
    public IdempotentInteresWriter(InteresRepository repository) { this.repository = repository; }
    @Override public void write(Chunk<? extends InteresTrimestral> chunk) {
        chunk.getItems().stream().filter(item -> !repository.existsByCuentaId(item.getCuentaId()))
                .forEach(repository::save);
    }
}
