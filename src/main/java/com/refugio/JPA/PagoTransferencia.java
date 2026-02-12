package com.refugio.JPA;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.envers.Audited;

@Entity
@Audited
@Table(name = "pagos_transferencia")
public class PagoTransferencia extends Pago {
    private String iban;
    private String referencia;
}
