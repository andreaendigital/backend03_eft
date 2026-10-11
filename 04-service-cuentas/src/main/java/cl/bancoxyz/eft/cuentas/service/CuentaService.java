package cl.bancoxyz.eft.cuentas.service;

import cl.bancoxyz.eft.cuentas.model.Cuenta;
import cl.bancoxyz.eft.cuentas.model.TipoCuenta;
import cl.bancoxyz.eft.cuentas.repository.CuentaRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CuentaService {
    private final CuentaRepository repository;

    public CuentaService(CuentaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Cuenta abrir(UUID clienteId, String numero, TipoCuenta tipo) {
        return repository.save(new Cuenta(clienteId, numero, tipo));
    }

    @Transactional
    public Cuenta cerrar(UUID id) {
        Cuenta cuenta = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada: " + id));
        cuenta.cerrar();
        return repository.save(cuenta);
    }

    @Transactional(readOnly = true)
    public List<Cuenta> listarPorCliente(UUID clienteId) {
        return repository.findByClienteId(clienteId);
    }
}
