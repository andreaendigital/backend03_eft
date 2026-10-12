package cl.bancoxyz.eft.batch.processor;

import cl.bancoxyz.eft.batch.model.cuentas.EstadoFinanciero;
import cl.bancoxyz.eft.batch.model.input.EstadoFinancieroInput;
import cl.bancoxyz.eft.batch.support.BatchDataException;
import cl.bancoxyz.eft.batch.support.DateNormalizer;
import java.math.BigDecimal;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class EstadoFinancieroProcessor implements ItemProcessor<EstadoFinancieroInput, EstadoFinanciero> {
    @Override
    public EstadoFinanciero process(EstadoFinancieroInput input) {
        if (input.getCuentaId() == null || input.getCuentaId().isBlank()) {
            throw new BatchDataException("Cuenta obligatoria vacía");
        }
        BigDecimal monto = optionalDecimal(input.getMonto());
        if (monto.signum() < 0) throw new BatchDataException("Monto negativo: " + input.getCuentaId());
        String transaccion = normalizeTransaction(input.getTransaccion());
        return new EstadoFinanciero(input.getCuentaId().trim(), DateNormalizer.parse(input.getFecha()),
                transaccion, monto, blankToNull(input.getDescripcion()));
    }

    private String normalizeTransaction(String value) {
        if (value == null || value.isBlank()) throw new BatchDataException("Transacción vacía");
        return value.trim().toLowerCase().replace("depósito", "deposito");
    }

    private BigDecimal optionalDecimal(String value) {
        if (value == null || value.isBlank()) return BigDecimal.ZERO;
        try { return new BigDecimal(value.trim()); }
        catch (NumberFormatException exception) { throw new BatchDataException("Monto inválido"); }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
