package cl.bancoxyz.eft.batch.model.pagos;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Interés calculado durante la migración ETL desde CSV legacy.
 * Almacena tanto el interés trimestral (calculado directamente sobre el saldo)
 * como el interés mensual equivalente (trimestral / 3), que da cumplimiento al
 * requerimiento de "Cálculo de Intereses Mensuales" del enunciado.
 * Persiste en banco_xyz_pagos.intereses_trimestrales.
 */
@Entity
@Table(name = "intereses_trimestrales",
       uniqueConstraints = @UniqueConstraint(columnNames = "cuenta_id"))
public class InteresTrimestral {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cuenta_id", nullable = false, unique = true)
    private String cuentaId;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo;

    @Column
    private Integer edad;

    /** Interés del trimestre completo: saldo × tasa_trimestral */
    @Column(name = "interes_calculado", nullable = false, precision = 19, scale = 2)
    private BigDecimal interes;

    /** Interés mensual equivalente: interes_calculado / 3 */
    @Column(name = "interes_mensual", nullable = false, precision = 19, scale = 2)
    private BigDecimal interesMensual;

    @Column(nullable = false, length = 20)
    private String tipo;

    protected InteresTrimestral() {}

    public InteresTrimestral(String cuentaId, String nombre, BigDecimal saldo,
                             Integer edad, BigDecimal interes, BigDecimal interesMensual, String tipo) {
        this.cuentaId      = cuentaId;
        this.nombre        = nombre;
        this.saldo         = saldo;
        this.edad          = edad;
        this.interes       = interes;
        this.interesMensual = interesMensual;
        this.tipo          = tipo;
    }

    public String getCuentaId()       { return cuentaId; }
    public BigDecimal getInteres()    { return interes; }
    public BigDecimal getInteresMensual() { return interesMensual; }
}
