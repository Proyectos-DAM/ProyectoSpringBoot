package com.refugio.controller;

import com.refugio.JPA.Suscripcion;
import com.refugio.service.SuscripcionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controlador de suscripciones
@Controller
@RequestMapping("/suscripciones")
public class SuscripcionController {

    private final SuscripcionService suscripcionService;

    public SuscripcionController(SuscripcionService suscripcionService) {
        this.suscripcionService = suscripcionService;
    }

    // Lista suscripciones
    @GetMapping
    public String listarSuscripciones(Model model) {
        List<Suscripcion> suscripciones = suscripcionService.obtenerTodasLasSuscripciones();
        model.addAttribute("suscripciones", suscripciones);
        model.addAttribute("titulo", "Gestión de Suscripciones");
        return "suscripciones/lista";
    }

    // Detalle de suscripción
    @GetMapping("/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        Suscripcion suscripcion = suscripcionService.obtenerSuscripcion(id);
        model.addAttribute("suscripcion", suscripcion);
        return "suscripciones/detalle";
    }

    // Activa suscripción
    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Long id) {
        suscripcionService.activarSuscripcion(id);
        return "redirect:/suscripciones/" + id;
    }

    // Cancela suscripción
    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id) {
        suscripcionService.cancelarSuscripcion(id);
        return "redirect:/suscripciones/" + id;
    }

    // Toggle renovación automática
    @PostMapping("/{id}/renovacion-automatica")
    public String toggleRenovacion(@PathVariable Long id, @RequestParam boolean habilitar) {
        suscripcionService.configurarRenovacionAutomatica(id, habilitar);
        return "redirect:/suscripciones/" + id;
    }

    // Renueva manualmente
    @PostMapping("/{id}/renovar")
    public String renovarManualmente(@PathVariable Long id) {
        Suscripcion suscripcion = suscripcionService.obtenerSuscripcion(id);
        suscripcionService.renovarSuscripcion(suscripcion);
        return "redirect:/suscripciones/" + id;
    }

    // Próximas a vencer
    @GetMapping("/proximas-vencer")
    public String proximasAVencer(@RequestParam(defaultValue = "7") int dias, Model model) {
        List<Suscripcion> suscripciones = suscripcionService.obtenerSuscripcionesProximasAVencer(dias);
        model.addAttribute("suscripciones", suscripciones);
        model.addAttribute("titulo", "Suscripciones próximas a vencer (próximos " + dias + " días)");
        return "suscripciones/lista";
    }

    // Filtra por estado
    @GetMapping("/estado/{estado}")
    public String porEstado(@PathVariable String estado, Model model) {
        Suscripcion.EstadoSuscripcion estadoEnum;
        try {
            estadoEnum = Suscripcion.EstadoSuscripcion.valueOf(estado.toUpperCase());
        } catch (IllegalArgumentException e) {
            return "redirect:/suscripciones";
        }

        List<Suscripcion> suscripciones = suscripcionService.obtenerTodasLasSuscripciones().stream()
                .filter(s -> s.getEstado() == estadoEnum)
                .toList();

        model.addAttribute("suscripciones", suscripciones);
        model.addAttribute("titulo", "Suscripciones - Estado: " + estado);
        return "suscripciones/lista";
    }
}
