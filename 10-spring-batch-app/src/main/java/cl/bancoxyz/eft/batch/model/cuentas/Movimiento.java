package cl.bancoxyz.eft.batch.model.cuentas;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Registro de movimiento financiero diario migrado desde CSV legacy.
 * Persiste en banco_xyz_cuentas.movimientos_financieros_diarios.
 */
@Entity
@Table(name = "movimientos_financieros_diarios",
       uniqueConstraints = @UniqueConstraint(columnNames = "source_id"))
public class Movimiento {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_id", nullable = false, unique = true)
    private String sourceId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 20)
    private String tipo;

    protected Movimiento() {}

    public Movimiento(String sourceId, LocalDate fecha, BigDecimal monto, String tipo) {
        this.sourceId = sourceId;
        this.fecha    = fecha;
        this.monto    = monto;
        this.tipo     = tipo;
    }

    public String getSourceId() { return sourceId; }
    public LocalDate getFecha() { return fecha; }
    public BigDecimal getMonto() { return monto; }
    public String getTipo() { return tipo; }
}
