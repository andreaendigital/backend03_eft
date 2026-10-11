package cl.bancoxyz.eft.batch.support;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public final class DateNormalizer {
    private static final List<DateTimeFormatter> FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"));

    private DateNormalizer() { }

    public static LocalDate parse(String value) {
        if (value == null || value.isBlank()) {
            throw new BatchDataException("Fecha obligatoria vacía");
        }
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDate.parse(value.trim(), formatter);
            } catch (DateTimeParseException ignored) {
                // Try the next legacy format.
            }
        }
        throw new BatchDataException("Formato de fecha inválido: " + value);
    }
}
