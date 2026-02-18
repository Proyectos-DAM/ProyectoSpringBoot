package com.refugio.service;

import com.refugio.JPA.Factura;
import com.refugio.JPA.Perfil;
import com.refugio.JPA.Suscripcion;
import com.refugio.repository.FacturaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class FacturaService {

    private static final Logger logger = LoggerFactory.getLogger(FacturaService.class);

    private final FacturaRepository facturaRepository;
    private final ImpuestoService impuestoService;

    public FacturaService(FacturaRepository facturaRepository, ImpuestoService impuestoService) {
        this.facturaRepository = facturaRepository;
        this.impuestoService = impuestoService;
    }

    // Genera factura con impuestos según país
    @Transactional
    public Factura generarFactura(Suscripcion suscripcion) {
        BigDecimal precioBase = suscripcion.getPlan().getPrecioMensual();
        String pais = obtenerPaisUsuario(suscripcion);
        ImpuestoService.DetalleImpuesto detalle = impuestoService.calcularDetalleImpuesto(precioBase, pais);

        Factura factura = new Factura();
        factura.setSuscripcion(suscripcion);
        factura.setFechaEmision(LocalDate.now());
        factura.setImporte(precioBase);
        factura.setImporteImpuesto(detalle.getMontoImpuesto());
        factura.setImporteTotal(detalle.getMontoTotal());
        factura.setPaisImpuesto(pais);
        factura.setTasaImpuesto(detalle.getTasaPorcentaje());
        factura.setEstado(Factura.EstadoFactura.EMITIDA);

        factura = facturaRepository.save(factura);

        logger.info("Factura generada: {} - Base: {}, Impuesto ({}%): {}, Total: {}",
                factura.getId(), precioBase, detalle.getTasaPorcentaje(),
                detalle.getMontoImpuesto(), detalle.getMontoTotal());

        return factura;
    }

    // Obtiene país del usuario
    private String obtenerPaisUsuario(Suscripcion suscripcion) {
        Perfil perfil = suscripcion.getUsuario().getPerfil();
        if (perfil != null && perfil.getPais() != null && !perfil.getPais().isBlank()) {
            return perfil.getPais();
        }
        return "ES";
    }

    // Marca factura como pagada
    @Transactional
    public Factura marcarComoPagada(Long facturaId) {
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada: " + facturaId));

        factura.setEstado(Factura.EstadoFactura.PAGADA);
        logger.info("Factura {} marcada como pagada", facturaId);

        return facturaRepository.save(factura);
    }

    // Anula factura
    @Transactional
    public Factura anularFactura(Long facturaId) {
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada: " + facturaId));

        factura.setEstado(Factura.EstadoFactura.ANULADA);
        logger.info("Factura {} anulada", facturaId);

        return facturaRepository.save(factura);
    }

    // Obtiene todas las facturas
    public List<Factura> obtenerTodasLasFacturas() {
        return facturaRepository.findAllByOrderByFechaEmisionDesc();
    }

    // Obtiene factura por ID
    public Factura obtenerFactura(Long id) {
        return facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada: " + id));
    }

    // Filtra por fechas
    public List<Factura> filtrarPorFecha(LocalDate fechaInicio, LocalDate fechaFin) {
        return facturaRepository.findByFechaEmisionBetween(fechaInicio, fechaFin);
    }

    // Filtra por monto
    public List<Factura> filtrarPorMonto(BigDecimal montoMinimo, BigDecimal montoMaximo) {
        return facturaRepository.findByImporteTotalBetween(montoMinimo, montoMaximo);
    }

    // Filtra por múltiples criterios
    public List<Factura> filtrarFacturas(LocalDate fechaInicio, LocalDate fechaFin,
                                         BigDecimal montoMinimo, BigDecimal montoMaximo,
                                         Factura.EstadoFactura estado) {
        return facturaRepository.findByFiltros(fechaInicio, fechaFin, montoMinimo, montoMaximo, estado);
    }

    // Obtiene facturas de un usuario
    public List<Factura> obtenerFacturasUsuario(Long usuarioId) {
        return facturaRepository.findByUsuarioId(usuarioId);
    }

    // Obtiene facturas por estado
    public List<Factura> obtenerFacturasPorEstado(Factura.EstadoFactura estado) {
        return facturaRepository.findByEstado(estado);
    }

    // Obtiene facturas pendientes
    public List<Factura> obtenerFacturasPendientes() {
        return facturaRepository.findByEstadoOrderByFechaEmisionAsc(Factura.EstadoFactura.EMITIDA);
    }

    // Recalcula impuesto de factura
    @Transactional
    public Factura recalcularImpuesto(Long facturaId, String nuevoPais) {
        Factura factura = obtenerFactura(facturaId);

        ImpuestoService.DetalleImpuesto detalle =
                impuestoService.calcularDetalleImpuesto(factura.getImporte(), nuevoPais);

        factura.setImporteImpuesto(detalle.getMontoImpuesto());
        factura.setImporteTotal(detalle.getMontoTotal());
        factura.setPaisImpuesto(nuevoPais);
        factura.setTasaImpuesto(detalle.getTasaPorcentaje());

        logger.info("Impuesto recalculado para factura {}: nuevo país {}, nueva tasa {}%",
                facturaId, nuevoPais, detalle.getTasaPorcentaje());

        return facturaRepository.save(factura);
    }
}
