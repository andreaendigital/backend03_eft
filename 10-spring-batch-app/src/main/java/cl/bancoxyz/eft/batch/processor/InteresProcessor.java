package cl.bancoxyz.eft.batch.processor;

import cl.bancoxyz.eft.batch.model.pagos.InteresTrimestral;
import cl.bancoxyz.eft.batch.model.input.InteresInput;
import cl.bancoxyz.eft.batch.support.BatchDataException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class InteresProcessor implements ItemProcessor<InteresInput, InteresTrimestral> {
    @Override
    public InteresTrimestral process(InteresInput input) {
        if (input.getCuentaId() == null || input.getCuentaId().isBlank()) {
            throw new BatchDataException("Cuenta obligatoria vacía");
        }
        BigDecimal saldo = optionalDecimal(input.getSaldo());
        if (saldo.signum() < 0) throw new BatchDataException("Saldo negativo: " + input.getCuentaId());
        String tipo = normalizeType(input.getTipo());
        BigDecimal rate = switch (tipo) {
            case "ahorro" -> new BigDecimal("0.0125");
            case "hipoteca" -> new BigDecimal("0.0075");
            case "prestamo" -> new BigDecimal("0.0200");
            default -> new BigDecimal("0.0100");
        };
        Integer edad = parseAge(input.getEdad());
        BigDecimal interes = saldo.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        return new InteresTrimestral(input.getCuentaId().trim(),
                input.getNombre() == null || input.getNombre().isBlank() ? "SIN NOMBRE" : input.getNombre().trim(),
                saldo, edad, interes, tipo);
    }

    private String normalizeType(String value) {
        if (value == null || value.isBlank()) return "ahorro";
        String normalized = value.trim().toLowerCase();
        return switch (normalized) {
            case "ahorro", "hipoteca", "prestamo", "préstamo" -> normalized.replace("préstamo", "prestamo");
            default -> "ahorro";
        };
    }

    private BigDecimal optionalDecimal(String value) {
        if (value == null || value.isBlank()) return BigDecimal.ZERO;
        try { return new BigDecimal(value.trim()); }
        catch (NumberFormatException exception) { throw new BatchDataException("Saldo inválido"); }
    }

    private Integer parseAge(String value) {
        if (value == null || value.isBlank()) return null;
        try { return Integer.valueOf(value.trim()); }
        catch (NumberFormatException exception) { throw new BatchDataException("Edad inválida"); }
    }
}
