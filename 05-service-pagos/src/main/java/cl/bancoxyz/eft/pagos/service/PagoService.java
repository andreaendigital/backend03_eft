package cl.bancoxyz.eft.pagos.service;

import cl.bancoxyz.eft.pagos.model.Pago;
import cl.bancoxyz.eft.pagos.model.TipoPago;
import cl.bancoxyz.eft.pagos.repository.PagoRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PagoService {
    private final PagoRepository repository;

    public PagoService(PagoRepository repository) { this.repository = repository; }

    @Transactional
    public Pago crear(UUID origen, UUID destino, BigDecimal monto, TipoPago tipo) {
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero");
        }
        return repository.save(new Pago(origen, destino, monto, tipo));
    }
}
