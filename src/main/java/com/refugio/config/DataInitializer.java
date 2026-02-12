package com.refugio.config;

import com.refugio.JPA.*;
import com.refugio.repository.FacturaRepository;
import com.refugio.repository.PlanRepository;
import com.refugio.repository.SuscripcionRepository;
import com.refugio.repository.UsuarioRepository;
import com.refugio.service.ImpuestoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

// Inicializa datos de prueba al arrancar
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PlanRepository planRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final FacturaRepository facturaRepository;
    private final ImpuestoService impuestoService;

    public DataInitializer(UsuarioRepository usuarioRepository,
                          PlanRepository planRepository,
                          SuscripcionRepository suscripcionRepository,
                          FacturaRepository facturaRepository,
                          ImpuestoService impuestoService) {
        this.usuarioRepository = usuarioRepository;
        this.planRepository = planRepository;
        this.suscripcionRepository = suscripcionRepository;
        this.facturaRepository = facturaRepository;
        this.impuestoService = impuestoService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        logger.info("Inicializando datos de prueba...");

        try {
            if (planRepository.count() == 0) {
                crearPlanes();
            }
            if (usuarioRepository.count() == 0) {
                crearUsuariosDePrueba();
            }
            logger.info("Datos de prueba inicializados correctamente");
        } catch (Exception e) {
            logger.error("Error al inicializar datos: {}", e.getMessage());
        }
    }

    private void crearPlanes() {
        Plan planBasic = new Plan();
        planBasic.setTipo(Plan.TipoPlan.BASIC);
        planBasic.setNombre("Plan Básico");
        planBasic.setPrecioMensual(new BigDecimal("9.99"));
        planRepository.save(planBasic);

        Plan planPremium = new Plan();
        planPremium.setTipo(Plan.TipoPlan.PREMIUM);
        planPremium.setNombre("Plan Premium");
        planPremium.setPrecioMensual(new BigDecimal("19.99"));
        planRepository.save(planPremium);

        Plan planEnterprise = new Plan();
        planEnterprise.setTipo(Plan.TipoPlan.ENTERPRISE);
        planEnterprise.setNombre("Plan Enterprise");
        planEnterprise.setPrecioMensual(new BigDecimal("49.99"));
        planRepository.save(planEnterprise);

        logger.info("Planes creados: BASIC, PREMIUM, ENTERPRISE");
    }

    private void crearUsuariosDePrueba() {
        Usuario usuario1 = crearUsuario("carlos@ejemplo.com", "Carlos García", "ES");
        Plan planPremium = planRepository.findByTipo(Plan.TipoPlan.PREMIUM).orElseThrow();
        crearSuscripcionConFactura(usuario1, planPremium, true);

        Usuario usuario2 = crearUsuario("anna@beispiel.de", "Anna Müller", "DE");
        Plan planBasic = planRepository.findByTipo(Plan.TipoPlan.BASIC).orElseThrow();
        crearSuscripcionConFactura(usuario2, planBasic, true);

        Usuario usuario3 = crearUsuario("pierre@exemple.fr", "Pierre Dupont", "FR");
        crearSuscripcionConFactura(usuario3, planPremium, false);

        Usuario usuario4 = crearUsuario("maria@ejemplo.mx", "María López", "MX");
        Plan planEnterprise = planRepository.findByTipo(Plan.TipoPlan.ENTERPRISE).orElseThrow();
        crearSuscripcionConFactura(usuario4, planEnterprise, true);

        Usuario usuario5 = crearUsuario("john@example.com", "John Smith", "US");
        crearSuscripcionConFactura(usuario5, planBasic, true);

        logger.info("Usuarios de prueba creados");
    }

    private Usuario crearUsuario(String email, String nombre, String pais) {
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPasswordHash("hashed_password_123");

        Perfil perfil = new Perfil();
        perfil.setNombre(nombre);
        perfil.setPais(pais);
        perfil.setUsuario(usuario);
        usuario.setPerfil(perfil);

        return usuarioRepository.save(usuario);
    }

    private void crearSuscripcionConFactura(Usuario usuario, Plan plan, boolean renovacionAutomatica) {
        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setUsuario(usuario);
        suscripcion.setPlan(plan);
        suscripcion.setFechaInicio(LocalDate.now());
        suscripcion.setProximaRenovacion(LocalDate.now().plusMonths(1));
        suscripcion.setRenovacionAutomatica(renovacionAutomatica);
        suscripcion.setEstado(Suscripcion.EstadoSuscripcion.ACTIVA);
        suscripcion = suscripcionRepository.save(suscripcion);

        String pais = usuario.getPerfil().getPais();
        BigDecimal precioBase = plan.getPrecioMensual();
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
        facturaRepository.save(factura);

        logger.info("Suscripción y factura creadas para: {}", usuario.getEmail());
    }
}

