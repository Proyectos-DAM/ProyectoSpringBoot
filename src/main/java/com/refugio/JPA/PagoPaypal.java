package com.refugio.JPA;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.envers.Audited;

@Entity
@Audited
@Table(name = "pagos_paypal")
public class PagoPaypal extends Pago {
    private String paypalEmail;
    private String transaccionId;
}
