package com.refugio.JPA;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;

@Entity
@Audited
@Table(name = "planes")
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private TipoPlan tipo; // BASIC, PREMIUM, ENTERPRISE

    @Column(nullable = false)
    private BigDecimal precioMensual;

    @Column(nullable = false)
    private String nombre;

    public enum TipoPlan { BASIC, PREMIUM, ENTERPRISE }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TipoPlan getTipo() { return tipo; }
    public void setTipo(TipoPlan tipo) { this.tipo = tipo; }

    public BigDecimal getPrecioMensual() { return precioMensual; }
    public void setPrecioMensual(BigDecimal precioMensual) { this.precioMensual = precioMensual; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}

