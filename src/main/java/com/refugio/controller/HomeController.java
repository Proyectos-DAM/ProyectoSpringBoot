package com.refugio.controller;

import com.refugio.JPA.Factura;
import com.refugio.JPA.Suscripcion;
import com.refugio.service.FacturaService;
import com.refugio.service.SuscripcionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

// Controlador principal - Dashboard
@Controller
public class HomeController {

    private final SuscripcionService suscripcionService;
    private final FacturaService facturaService;

    public HomeController(SuscripcionService suscripcionService, FacturaService facturaService) {
        this.suscripcionService = suscripcionService;
        this.facturaService = facturaService;
    }

    @GetMapping("/")
    public String home(Model model) {
        // Suscripciones
        List<Suscripcion> todas = suscripcionService.obtenerTodasLasSuscripciones();
        long activas = todas.stream()
            .filter(s -> s.getEstado() == Suscripcion.EstadoSuscripcion.ACTIVA).count();
        long impagos = todas.stream()
            .filter(s -> s.getEstado() == Suscripcion.EstadoSuscripcion.IMPAGO).count();

        // Proximas a vencer
        List<Suscripcion> proximasVencer = suscripcionService.obtenerSuscripcionesProximasAVencer(7);

        // Facturas
        List<Factura> facturas = facturaService.obtenerTodasLasFacturas();
        long pendientes = facturas.stream()
            .filter(f -> f.getEstado() == Factura.EstadoFactura.EMITIDA).count();

        model.addAttribute("totalSuscripciones", todas.size());
        model.addAttribute("suscripcionesActivas", activas);
        model.addAttribute("suscripcionesImpago", impagos);
        model.addAttribute("proximasVencer", proximasVencer.size());
        model.addAttribute("totalFacturas", facturas.size());
        model.addAttribute("facturasPendientes", pendientes);

        return "index";
    }
}

