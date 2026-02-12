package com.refugio.JPA;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.envers.Audited;

@Entity
@Audited
@Table(name = "pagos_tarjeta")
public class PagoTarjeta extends Pago {
    private String ultimos4;
    private String marca;
}
