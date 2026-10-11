package cl.bancoxyz.eft.clientes.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, unique = true, length = 20)
    private String rut;
    @Column(nullable = false, length = 100)
    private String nombres;
    @Column(nullable = false, length = 100)
    private String apellidos;
    @Column(nullable = false, unique = true, length = 150)
    private String email;
    @Column(length = 30)
    private String telefono;
    @Column(nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    protected Cliente() { }

    public Cliente(String rut, String nombres, String apellidos, String email, String telefono) {
        this.rut = rut;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.email = email;
        this.telefono = telefono;
        this.creadoEn = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public String getRut() { return rut; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
}
