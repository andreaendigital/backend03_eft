package cl.bancoxyz.eft.batch.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "batch_estados_financieros",
       uniqueConstraints = @UniqueConstraint(columnNames = {"cuentaId", "fecha", "transaccion"}))
public class EstadoFinanciero {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String cuentaId;
    @Column(nullable = false) private LocalDate fecha;
    @Column(nullable = false, length = 20) private String transaccion;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal monto;
    @Column(length = 255) private String descripcion;
    protected EstadoFinanciero() { }
    public EstadoFinanciero(String cuentaId, LocalDate fecha, String transaccion,
                            BigDecimal monto, String descripcion) {
        this.cuentaId = cuentaId; this.fecha = fecha; this.transaccion = transaccion;
        this.monto = monto; this.descripcion = descripcion;
    }
    public String getCuentaId() { return cuentaId; }
    public LocalDate getFecha() { return fecha; }
    public String getTransaccion() { return transaccion; }
}
