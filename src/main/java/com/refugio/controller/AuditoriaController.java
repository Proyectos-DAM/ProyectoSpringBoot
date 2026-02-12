package com.refugio.controller;

import com.refugio.JPA.Suscripcion;
import com.refugio.JPA.Usuario;
import com.refugio.service.AuditoriaService;
import com.refugio.service.AuditoriaService.RegistroAuditoria;
import com.refugio.service.AuditoriaService.ResumenCambio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controlador para panel de auditoría
@Controller
@RequestMapping("/admin/auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    // Panel principal
    @GetMapping
    public String mostrarPanelAuditoria(Model model) {
        List<ResumenCambio> cambiosRecientes = auditoriaService.obtenerCambiosRecientes(50);
        model.addAttribute("cambios", cambiosRecientes);
        model.addAttribute("titulo", "Panel de Auditoría");
        return "admin/auditoria/panel";
    }

    // Historial de usuario
    @GetMapping("/usuario/{id}")
    public String verHistorialUsuario(@PathVariable Long id, Model model) {
        List<RegistroAuditoria<Usuario>> historial =
                auditoriaService.obtenerHistorial(Usuario.class, id);

        model.addAttribute("historial", historial);
        model.addAttribute("tipoEntidad", "Usuario");
        model.addAttribute("entityId", id);
        model.addAttribute("titulo", "Historial de Usuario #" + id);

        return "admin/auditoria/historial";
    }

    // Historial de suscripción
    @GetMapping("/suscripcion/{id}")
    public String verHistorialSuscripcion(@PathVariable Long id, Model model) {
        List<RegistroAuditoria<Suscripcion>> historial =
                auditoriaService.obtenerHistorial(Suscripcion.class, id);

        model.addAttribute("historial", historial);
        model.addAttribute("tipoEntidad", "Suscripción");
        model.addAttribute("entityId", id);
        model.addAttribute("titulo", "Historial de Suscripción #" + id);

        return "admin/auditoria/historial";
    }

    // Revisión de usuario
    @GetMapping("/usuario/{id}/revision/{revisionId}")
    public String verRevisionUsuario(@PathVariable Long id,
                                     @PathVariable Long revisionId,
                                     Model model) {
        Usuario usuario = auditoriaService.obtenerVersionAnterior(Usuario.class, id, revisionId);

        model.addAttribute("entidad", usuario);
        model.addAttribute("tipoEntidad", "Usuario");
        model.addAttribute("numeroRevision", revisionId);
        model.addAttribute("titulo", "Usuario #" + id + " - Revisión #" + revisionId);

        return "admin/auditoria/revision-usuario";
    }

    // Revisión de suscripción
    @GetMapping("/suscripcion/{id}/revision/{revisionId}")
    public String verRevisionSuscripcion(@PathVariable Long id,
                                         @PathVariable Long revisionId,
                                         Model model) {
        Suscripcion suscripcion = auditoriaService.obtenerVersionAnterior(Suscripcion.class, id, revisionId);

        model.addAttribute("entidad", suscripcion);
        model.addAttribute("tipoEntidad", "Suscripción");
        model.addAttribute("numeroRevision", revisionId);
        model.addAttribute("titulo", "Suscripción #" + id + " - Revisión #" + revisionId);

        return "admin/auditoria/revision-suscripcion";
    }

    // Búsqueda de historial
    @GetMapping("/buscar")
    public String buscarHistorial(
            @RequestParam String tipoEntidad,
            @RequestParam Long entityId,
            Model model) {

        switch (tipoEntidad.toLowerCase()) {
            case "usuario":
                return "redirect:/admin/auditoria/usuario/" + entityId;
            case "suscripcion":
                return "redirect:/admin/auditoria/suscripcion/" + entityId;
            default:
                model.addAttribute("error", "Tipo de entidad no válido");
                return mostrarPanelAuditoria(model);
        }
    }
}
