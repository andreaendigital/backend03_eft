package cl.bancoxyz.eft.pagos.service;

import cl.bancoxyz.eft.pagos.model.Pago;
import cl.bancoxyz.eft.pagos.model.TipoPago;
import cl.bancoxyz.eft.pagos.repository.PagoRepository;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PagoWorkflowService {
    private final PagoRepository repository;
    private final AccountFundsClient accounts;

    public PagoWorkflowService(PagoRepository repository, AccountFundsClient accounts) {
        this.repository = repository;
        this.accounts = accounts;
    }

    @Transactional
    public Pago process(UUID origen, UUID destino, BigDecimal monto, TipoPago tipo) {
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero");
        }
        UUID workflowId = UUID.randomUUID();
        try {
            if (!accounts.reserve(origen, monto, workflowId).get(3, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Fondos insuficientes para reservar el pago");
            }
            Pago payment = repository.save(new Pago(origen, destino, monto, tipo));
            accounts.confirm(origen, workflowId).get(3, TimeUnit.SECONDS);
            payment.completar();
            return repository.save(payment);
        } catch (Exception failure) {
            try {
                accounts.release(origen, workflowId).get(3, TimeUnit.SECONDS);
            } catch (Exception compensationFailure) {
                throw new IllegalStateException("Falló el pago y también su compensación", compensationFailure);
            }
            throw new IllegalStateException("Pago revertido por fallo en el workflow", failure);
        }
    }
}
