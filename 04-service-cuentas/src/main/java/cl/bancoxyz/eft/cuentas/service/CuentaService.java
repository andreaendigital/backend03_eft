package cl.bancoxyz.eft.cuentas.service;

import cl.bancoxyz.eft.cuentas.model.Cuenta;
import cl.bancoxyz.eft.cuentas.model.TipoCuenta;
import cl.bancoxyz.eft.cuentas.messaging.KafkaEventPublisher;
import cl.bancoxyz.eft.cuentas.repository.CuentaRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CuentaService {
    private final CuentaRepository repository;
    private final KafkaEventPublisher events;

    public CuentaService(CuentaRepository repository, KafkaEventPublisher events) {
        this.repository = repository;
        this.events = events;
    }

    @Transactional
    public Cuenta abrir(UUID clienteId, String numero, TipoCuenta tipo) {
        Cuenta cuenta = repository.save(new Cuenta(clienteId, numero, tipo));
        events.accountChanged(cuenta);
        return cuenta;
    }

    @Transactional
    public Cuenta cerrar(UUID id) {
        Cuenta cuenta = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada: " + id));
        cuenta.cerrar();
        Cuenta updated = repository.save(cuenta);
        events.accountChanged(updated);
        return updated;
    }

    @Transactional(readOnly = true)
    public List<Cuenta> listarPorCliente(UUID clienteId) {
        return repository.findByClienteId(clienteId);
    }
}
