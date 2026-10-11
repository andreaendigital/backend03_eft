package cl.bancoxyz.eft.batch.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "batch_intereses", uniqueConstraints = @UniqueConstraint(columnNames = "cuentaId"))
public class InteresTrimestral {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true) private String cuentaId;
    @Column(nullable = false) private String nombre;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal saldo;
    @Column(nullable = false, precision = 8, scale = 5) private BigDecimal interes;
    @Column(nullable = false, length = 20) private String tipo;
    protected InteresTrimestral() { }
    public InteresTrimestral(String cuentaId, String nombre, BigDecimal saldo,
                             BigDecimal interes, String tipo) {
        this.cuentaId = cuentaId; this.nombre = nombre; this.saldo = saldo;
        this.interes = interes; this.tipo = tipo;
    }
    public String getCuentaId() { return cuentaId; }
}
