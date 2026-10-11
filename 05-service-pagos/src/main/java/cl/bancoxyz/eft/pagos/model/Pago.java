package cl.bancoxyz.eft.pagos.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pagos")
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private UUID cuentaOrigenId;
    private UUID cuentaDestinoId;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal monto;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoPago tipo;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPago estado = EstadoPago.PENDIENTE;
    @Column(nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    protected Pago() { }

    public Pago(UUID cuentaOrigenId, UUID cuentaDestinoId, BigDecimal monto, TipoPago tipo) {
        this.cuentaOrigenId = cuentaOrigenId;
        this.cuentaDestinoId = cuentaDestinoId;
        this.monto = monto;
        this.tipo = tipo;
        this.creadoEn = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getCuentaOrigenId() { return cuentaOrigenId; }
    public UUID getCuentaDestinoId() { return cuentaDestinoId; }
    public BigDecimal getMonto() { return monto; }
    public TipoPago getTipo() { return tipo; }
    public EstadoPago getEstado() { return estado; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void completar() { this.estado = EstadoPago.COMPLETADO; }
    public void rechazar() { this.estado = EstadoPago.RECHAZADO; }
}
