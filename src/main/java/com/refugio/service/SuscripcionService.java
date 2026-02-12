package com.refugio.service;

import com.refugio.JPA.Factura;
import com.refugio.JPA.Plan;
import com.refugio.JPA.Suscripcion;
import com.refugio.JPA.Usuario;
import com.refugio.repository.FacturaRepository;
import com.refugio.repository.SuscripcionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

// Servicio para gestionar suscripciones y renovación automática
@Service
public class SuscripcionService {

    private static final Logger logger = LoggerFactory.getLogger(SuscripcionService.class);

    private final SuscripcionRepository suscripcionRepository;
    private final FacturaRepository facturaRepository;
    private final FacturaService facturaService;

    public SuscripcionService(SuscripcionRepository suscripcionRepository,
                              FacturaRepository facturaRepository,
                              @Lazy FacturaService facturaService) {
        this.suscripcionRepository = suscripcionRepository;
        this.facturaRepository = facturaRepository;
        this.facturaService = facturaService;
    }

    // Crea nueva suscripción
    @Transactional
    public Suscripcion crearSuscripcion(Usuario usuario, Plan plan, boolean renovacionAutomatica) {
        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setUsuario(usuario);
        suscripcion.setPlan(plan);
        suscripcion.setFechaInicio(LocalDate.now());
        suscripcion.setProximaRenovacion(LocalDate.now().plusMonths(1));
        suscripcion.setRenovacionAutomatica(renovacionAutomatica);
        suscripcion.setEstado(Suscripcion.EstadoSuscripcion.ACTIVA);

        suscripcion = suscripcionRepository.save(suscripcion);
        facturaService.generarFactura(suscripcion);

        logger.info("Nueva suscripción creada: {} para usuario {}", suscripcion.getId(), usuario.getEmail());
        return suscripcion;
    }

    // Activa suscripción
    @Transactional
    public Suscripcion activarSuscripcion(Long suscripcionId) {
        Suscripcion suscripcion = suscripcionRepository.findById(suscripcionId)
                .orElseThrow(() -> new RuntimeException("Suscripción no encontrada: " + suscripcionId));

        suscripcion.setEstado(Suscripcion.EstadoSuscripcion.ACTIVA);
        suscripcion.setFechaFin(null);

        logger.info("Suscripción activada: {}", suscripcionId);
        return suscripcionRepository.save(suscripcion);
    }

    // Cancela suscripción
    @Transactional
    public Suscripcion cancelarSuscripcion(Long suscripcionId) {
        Suscripcion suscripcion = suscripcionRepository.findById(suscripcionId)
                .orElseThrow(() -> new RuntimeException("Suscripción no encontrada: " + suscripcionId));

        suscripcion.setEstado(Suscripcion.EstadoSuscripcion.CANCELADA);
        suscripcion.setFechaFin(LocalDate.now());
        suscripcion.setRenovacionAutomatica(false);

        logger.info("Suscripción cancelada: {}", suscripcionId);
        return suscripcionRepository.save(suscripcion);
    }

    // Marca como impago
    @Transactional
    public Suscripcion marcarComoImpago(Long suscripcionId) {
        Suscripcion suscripcion = suscripcionRepository.findById(suscripcionId)
                .orElseThrow(() -> new RuntimeException("Suscripción no encontrada: " + suscripcionId));

        suscripcion.setEstado(Suscripcion.EstadoSuscripcion.IMPAGO);

        logger.warn("Suscripción marcada como impago: {}", suscripcionId);
        return suscripcionRepository.save(suscripcion);
    }

    // Configura renovación automática
    @Transactional
    public Suscripcion configurarRenovacionAutomatica(Long suscripcionId, boolean habilitar) {
        Suscripcion suscripcion = suscripcionRepository.findById(suscripcionId)
                .orElseThrow(() -> new RuntimeException("Suscripción no encontrada: " + suscripcionId));

        suscripcion.setRenovacionAutomatica(habilitar);

        logger.info("Renovación automática {} para suscripción: {}",
                habilitar ? "habilitada" : "deshabilitada", suscripcionId);
        return suscripcionRepository.save(suscripcion);
    }

    // Cambia el plan
    @Transactional
    public Suscripcion cambiarPlan(Long suscripcionId, Plan nuevoPlan) {
        Suscripcion suscripcion = suscripcionRepository.findById(suscripcionId)
                .orElseThrow(() -> new RuntimeException("Suscripción no encontrada: " + suscripcionId));

        suscripcion.setPlan(nuevoPlan);

        logger.info("Plan cambiado para suscripción {}: nuevo plan {}", suscripcionId, nuevoPlan.getTipo());
        return suscripcionRepository.save(suscripcion);
    }

    // Renovación automática diaria a las 2:00
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void procesarRenovacionesAutomaticas() {
        logger.info("Iniciando proceso de renovación automática...");

        LocalDate hoy = LocalDate.now();
        List<Suscripcion> suscripcionesARenovar = suscripcionRepository.findSuscripcionesParaRenovar(hoy);

        int renovadas = 0;
        int fallidas = 0;

        for (Suscripcion suscripcion : suscripcionesARenovar) {
            try {
                renovarSuscripcion(suscripcion);
                renovadas++;
            } catch (Exception e) {
                logger.error("Error al renovar suscripción {}: {}", suscripcion.getId(), e.getMessage());
                fallidas++;
            }
        }

        logger.info("Renovación finalizada. Renovadas: {}, Fallidas: {}", renovadas, fallidas);
    }

    // Renueva una suscripción
    @Transactional
    public void renovarSuscripcion(Suscripcion suscripcion) {
        logger.info("Renovando suscripción: {}", suscripcion.getId());

        if (!suscripcion.getRenovacionAutomatica()) {
            logger.info("Suscripción {} no tiene renovación automática", suscripcion.getId());
            return;
        }

        if (suscripcion.getEstado() != Suscripcion.EstadoSuscripcion.ACTIVA) {
            logger.info("Suscripción {} no está activa", suscripcion.getId());
            return;
        }

        Factura nuevaFactura = facturaService.generarFactura(suscripcion);
        suscripcion.setProximaRenovacion(LocalDate.now().plusMonths(1));
        suscripcionRepository.save(suscripcion);

        logger.info("Suscripción {} renovada. Nueva factura: {}", suscripcion.getId(), nuevaFactura.getId());
    }

    // Marca suscripciones expiradas diariamente a las 3:00
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void procesarSuscripcionesExpiradas() {
        logger.info("Procesando suscripciones expiradas...");

        LocalDate hoy = LocalDate.now();
        List<Suscripcion> expiradas = suscripcionRepository.findSuscripcionesExpiradas(hoy);

        for (Suscripcion suscripcion : expiradas) {
            suscripcion.setEstado(Suscripcion.EstadoSuscripcion.EXPIRADA);
            suscripcion.setFechaFin(hoy);
            suscripcionRepository.save(suscripcion);
            logger.info("Suscripción {} marcada como expirada", suscripcion.getId());
        }

        logger.info("Expiración finalizada. {} suscripciones expiradas", expiradas.size());
    }

    // Marca suscripciones con impago diariamente a las 4:00
    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void procesarSuscripcionesConImpago() {
        logger.info("Verificando suscripciones con facturas vencidas...");

        List<Factura> facturasPendientes = facturaRepository
                .findByEstadoOrderByFechaEmisionAsc(Factura.EstadoFactura.EMITIDA);

        LocalDate limiteVencimiento = LocalDate.now().minusDays(30);

        for (Factura factura : facturasPendientes) {
            if (factura.getFechaEmision().isBefore(limiteVencimiento)) {
                Suscripcion suscripcion = factura.getSuscripcion();
                if (suscripcion.getEstado() == Suscripcion.EstadoSuscripcion.ACTIVA) {
                    suscripcion.setEstado(Suscripcion.EstadoSuscripcion.IMPAGO);
                    suscripcionRepository.save(suscripcion);
                    logger.warn("Suscripción {} marcada como impago por factura {} vencida",
                            suscripcion.getId(), factura.getId());
                }
            }
        }
    }

    // Obtiene suscripciones de un usuario
    public List<Suscripcion> obtenerSuscripcionesUsuario(Long usuarioId) {
        return suscripcionRepository.findByUsuarioId(usuarioId);
    }

    // Obtiene suscripciones activas de un usuario
    public List<Suscripcion> obtenerSuscripcionesActivas(Long usuarioId) {
        return suscripcionRepository.findByUsuarioIdAndEstado(usuarioId, Suscripcion.EstadoSuscripcion.ACTIVA);
    }

    // Obtiene suscripciones próximas a vencer
    public List<Suscripcion> obtenerSuscripcionesProximasAVencer(int dias) {
        LocalDate hoy = LocalDate.now();
        return suscripcionRepository.findSuscripcionesProximasAVencer(hoy, hoy.plusDays(dias));
    }

    // Obtiene todas las suscripciones
    public List<Suscripcion> obtenerTodasLasSuscripciones() {
        return suscripcionRepository.findAll();
    }

    // Obtiene suscripción por ID
    public Suscripcion obtenerSuscripcion(Long id) {
        return suscripcionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Suscripción no encontrada: " + id));
    }
}
