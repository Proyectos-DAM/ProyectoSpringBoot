package com.refugio.JPA;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Audited
@Table(name = "facturas",
        indexes = {
                @Index(name = "idx_fact_sus_fecha", columnList = "suscripcion_id, fechaEmision")
        }
)
public class Factura {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suscripcion_id", nullable = false)
    private Suscripcion suscripcion;

    @Column(nullable = false)
    private LocalDate fechaEmision;

    @Column(nullable = false)
    private BigDecimal importe;

    @Column(nullable = false)
    private BigDecimal importeImpuesto = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal importeTotal = BigDecimal.ZERO;

    @Column
    private String paisImpuesto;

    @Column
    private BigDecimal tasaImpuesto = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoFactura estado; // EMITIDA, PAGADA, ANULADA

    @OneToOne(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Pago pago;

    public enum EstadoFactura { EMITIDA, PAGADA, ANULADA }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Suscripcion getSuscripcion() { return suscripcion; }
    public void setSuscripcion(Suscripcion suscripcion) { this.suscripcion = suscripcion; }

    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }

    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal importe) { this.importe = importe; }

    public BigDecimal getImporteImpuesto() { return importeImpuesto; }
    public void setImporteImpuesto(BigDecimal importeImpuesto) { this.importeImpuesto = importeImpuesto; }

    public BigDecimal getImporteTotal() { return importeTotal; }
    public void setImporteTotal(BigDecimal importeTotal) { this.importeTotal = importeTotal; }

    public String getPaisImpuesto() { return paisImpuesto; }
    public void setPaisImpuesto(String paisImpuesto) { this.paisImpuesto = paisImpuesto; }

    public BigDecimal getTasaImpuesto() { return tasaImpuesto; }
    public void setTasaImpuesto(BigDecimal tasaImpuesto) { this.tasaImpuesto = tasaImpuesto; }

    public EstadoFactura getEstado() { return estado; }
    public void setEstado(EstadoFactura estado) { this.estado = estado; }

    public Pago getPago() { return pago; }
    public void setPago(Pago pago) { this.pago = pago; }
}
