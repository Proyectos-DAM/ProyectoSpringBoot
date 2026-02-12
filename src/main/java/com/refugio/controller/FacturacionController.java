package com.refugio.controller;

import com.refugio.JPA.Factura;
import com.refugio.service.FacturaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Controlador de facturación
@Controller
@RequestMapping("/facturacion")
public class FacturacionController {

    private final FacturaService facturaService;

    public FacturacionController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    // Lista todas las facturas
    @GetMapping
    public String mostrarFacturas(Model model) {
        List<Factura> facturas = facturaService.obtenerTodasLasFacturas();
        model.addAttribute("facturas", facturas);
        model.addAttribute("titulo", "Panel de Facturación");
        return "facturacion/lista";
    }

    // Filtra facturas
    @GetMapping("/filtrar")
    public String filtrarFacturas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) BigDecimal montoMinimo,
            @RequestParam(required = false) BigDecimal montoMaximo,
            @RequestParam(required = false) String estado,
            Model model) {

        Factura.EstadoFactura estadoFactura = null;
        if (estado != null && !estado.isBlank()) {
            try {
                estadoFactura = Factura.EstadoFactura.valueOf(estado);
            } catch (IllegalArgumentException e) {
                // Estado inválido
            }
        }

        List<Factura> facturas = facturaService.filtrarFacturas(
                fechaInicio, fechaFin, montoMinimo, montoMaximo, estadoFactura);

        model.addAttribute("facturas", facturas);
        model.addAttribute("titulo", "Facturas Filtradas");
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("montoMinimo", montoMinimo);
        model.addAttribute("montoMaximo", montoMaximo);
        model.addAttribute("estadoSeleccionado", estado);

        return "facturacion/lista";
    }

    // Detalle de factura
    @GetMapping("/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        Factura factura = facturaService.obtenerFactura(id);
        model.addAttribute("factura", factura);
        return "facturacion/detalle";
    }

    // Marca como pagada
    @PostMapping("/{id}/pagar")
    public String pagarFactura(@PathVariable Long id) {
        facturaService.marcarComoPagada(id);
        return "redirect:/facturacion";
    }

    // Anula factura
    @PostMapping("/{id}/anular")
    public String anularFactura(@PathVariable Long id) {
        facturaService.anularFactura(id);
        return "redirect:/facturacion";
    }

    // Muestra pendientes
    @GetMapping("/pendientes")
    public String mostrarPendientes(Model model) {
        List<Factura> facturas = facturaService.obtenerFacturasPendientes();
        model.addAttribute("facturas", facturas);
        model.addAttribute("titulo", "Facturas Pendientes de Pago");
        return "facturacion/lista";
    }

    // Estadísticas
    @GetMapping("/estadisticas")
    public String mostrarEstadisticas(Model model) {
        List<Factura> todasLasFacturas = facturaService.obtenerTodasLasFacturas();

        BigDecimal totalFacturado = todasLasFacturas.stream()
                .map(Factura::getImporteTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalImpuestos = todasLasFacturas.stream()
                .map(Factura::getImporteImpuesto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long facturasPagadas = todasLasFacturas.stream()
                .filter(f -> f.getEstado() == Factura.EstadoFactura.PAGADA)
                .count();

        long facturasPendientes = todasLasFacturas.stream()
                .filter(f -> f.getEstado() == Factura.EstadoFactura.EMITIDA)
                .count();

        model.addAttribute("totalFacturado", totalFacturado);
        model.addAttribute("totalImpuestos", totalImpuestos);
        model.addAttribute("facturasPagadas", facturasPagadas);
        model.addAttribute("facturasPendientes", facturasPendientes);
        model.addAttribute("totalFacturas", todasLasFacturas.size());

        return "facturacion/estadisticas";
    }
}
