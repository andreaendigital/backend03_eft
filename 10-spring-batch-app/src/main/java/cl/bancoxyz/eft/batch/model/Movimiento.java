package cl.bancoxyz.eft.batch.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "batch_movimientos", uniqueConstraints = @UniqueConstraint(columnNames = "sourceId"))
public class Movimiento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true) private String sourceId;
    @Column(nullable = false) private LocalDate fecha;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal monto;
    @Column(nullable = false, length = 20) private String tipo;
    protected Movimiento() { }
    public Movimiento(String sourceId, LocalDate fecha, BigDecimal monto, String tipo) {
        this.sourceId = sourceId; this.fecha = fecha; this.monto = monto; this.tipo = tipo;
    }
    public String getSourceId() { return sourceId; }
}
