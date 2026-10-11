package cl.bancoxyz.eft.cuentas.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cuentas")
public class Cuenta {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private UUID clienteId;
    @Column(nullable = false, unique = true, length = 30)
    private String numero;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCuenta tipo;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal saldo = BigDecimal.ZERO;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCuenta estado = EstadoCuenta.ACTIVA;
    @Column(nullable = false, updatable = false)
    private LocalDateTime creadaEn;
    private LocalDateTime cerradaEn;

    protected Cuenta() { }

    public Cuenta(UUID clienteId, String numero, TipoCuenta tipo) {
        this.clienteId = clienteId;
        this.numero = numero;
        this.tipo = tipo;
        this.creadaEn = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getClienteId() { return clienteId; }
    public String getNumero() { return numero; }
    public TipoCuenta getTipo() { return tipo; }
    public BigDecimal getSaldo() { return saldo; }
    public EstadoCuenta getEstado() { return estado; }
    public LocalDateTime getCreadaEn() { return creadaEn; }
    public LocalDateTime getCerradaEn() { return cerradaEn; }
    public void cerrar() {
        this.estado = EstadoCuenta.CERRADA;
        this.cerradaEn = LocalDateTime.now();
    }
}
