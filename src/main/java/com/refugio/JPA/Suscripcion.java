package com.refugio.JPA;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Entity
@Audited
@Table(name = "suscripciones",
        indexes = {
                @Index(name = "idx_sus_usuario_estado", columnList = "usuario_id, estado"),
                @Index(name = "idx_sus_fechas", columnList = "fechaInicio, fechaFin")
        }
)
public class Suscripcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSuscripcion estado; // ACTIVA, CANCELADA, IMPAGO, PENDIENTE_RENOVACION, EXPIRADA

    @Column(nullable = false)
    private LocalDate fechaInicio;

    private LocalDate fechaFin; // null => activa

    @Column(nullable = false)
    private Boolean renovacionAutomatica = true;

    private LocalDate proximaRenovacion;

    @OneToMany(mappedBy = "suscripcion", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Factura> facturas = new ArrayList<>();

    public enum EstadoSuscripcion { ACTIVA, CANCELADA, IMPAGO, PENDIENTE_RENOVACION, EXPIRADA }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }

    public EstadoSuscripcion getEstado() { return estado; }
    public void setEstado(EstadoSuscripcion estado) { this.estado = estado; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public Boolean getRenovacionAutomatica() { return renovacionAutomatica; }
    public void setRenovacionAutomatica(Boolean renovacionAutomatica) { this.renovacionAutomatica = renovacionAutomatica; }

    public LocalDate getProximaRenovacion() { return proximaRenovacion; }
    public void setProximaRenovacion(LocalDate proximaRenovacion) { this.proximaRenovacion = proximaRenovacion; }

    public List<Factura> getFacturas() { return facturas; }
    public void setFacturas(List<Factura> facturas) { this.facturas = facturas; }
}

