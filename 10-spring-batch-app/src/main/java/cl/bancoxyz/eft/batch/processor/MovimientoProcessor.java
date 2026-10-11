package cl.bancoxyz.eft.batch.processor;

import cl.bancoxyz.eft.batch.model.Movimiento;
import cl.bancoxyz.eft.batch.model.input.MovimientoInput;
import cl.bancoxyz.eft.batch.support.BatchDataException;
import cl.bancoxyz.eft.batch.support.DateNormalizer;
import java.math.BigDecimal;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class MovimientoProcessor implements ItemProcessor<MovimientoInput, Movimiento> {
    @Override
    public Movimiento process(MovimientoInput input) {
        if (input.getId() == null || input.getId().isBlank()) {
            throw new BatchDataException("ID de movimiento obligatorio");
        }
        BigDecimal monto = decimal(input.getMonto(), "Monto inválido: " + input.getId());
        if (monto.signum() <= 0) {
            throw new BatchDataException("Monto debe ser positivo: " + input.getId());
        }
        String tipo = input.getTipo() == null ? "" : input.getTipo().trim().toLowerCase();
        if (!tipo.equals("credito") && !tipo.equals("debito")) {
            throw new BatchDataException("Tipo de movimiento inválido: " + input.getId());
        }
        return new Movimiento(input.getId().trim(), DateNormalizer.parse(input.getFecha()),
                monto, tipo);
    }

    private BigDecimal decimal(String value, String message) {
        try {
            if (value == null || value.isBlank()) throw new NumberFormatException();
            return new BigDecimal(value.trim());
        } catch (NumberFormatException exception) {
            throw new BatchDataException(message);
        }
    }
}
