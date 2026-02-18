package com.refugio.service;

import com.refugio.JPA.*;
import com.refugio.repository.FacturaRepository;
import com.refugio.repository.SuscripcionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Test para SuscripcionService - ciclo de vida de suscripciones
@ExtendWith(MockitoExtension.class)
class SuscripcionServiceTest {

    @Mock
    private SuscripcionRepository suscripcionRepository;

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private FacturaService facturaService;

    @InjectMocks
    private SuscripcionService suscripcionService;

    private Usuario usuario;
    private Plan plan;
    private Suscripcion suscripcion;

    @BeforeEach
    void setUp() {
        // Crear usuario de prueba
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("test@test.com");

        Perfil perfil = new Perfil();
        perfil.setPais("ES");
        usuario.setPerfil(perfil);

        // Crear plan de prueba
        plan = new Plan();
        plan.setId(1L);
        plan.setTipo(Plan.TipoPlan.BASIC);
        plan.setNombre("Plan Básico");
        plan.setPrecioMensual(new BigDecimal("9.99"));

        // Crear suscripción de prueba
        suscripcion = new Suscripcion();
        suscripcion.setId(1L);
        suscripcion.setUsuario(usuario);
        suscripcion.setPlan(plan);
        suscripcion.setEstado(Suscripcion.EstadoSuscripcion.ACTIVA);
        suscripcion.setFechaInicio(LocalDate.now());
        suscripcion.setRenovacionAutomatica(true);
        suscripcion.setProximaRenovacion(LocalDate.now().plusMonths(1));
    }

    // Tests de creación de suscripción
    @Test
    @DisplayName("Crear suscripción correctamente")
    void crearSuscripcion() {
        when(suscripcionRepository.save(any(Suscripcion.class)))
            .thenAnswer(inv -> {
                Suscripcion s = inv.getArgument(0);
                s.setId(1L);
                return s;
            });
        when(facturaService.generarFactura(any(Suscripcion.class)))
            .thenReturn(new Factura());

        Suscripcion resultado = suscripcionService.crearSuscripcion(usuario, plan, true);

        assertAll(
            () -> assertNotNull(resultado),
            () -> assertEquals(usuario, resultado.getUsuario()),
            () -> assertEquals(plan, resultado.getPlan()),
            () -> assertEquals(Suscripcion.EstadoSuscripcion.ACTIVA, resultado.getEstado()),
            () -> assertTrue(resultado.getRenovacionAutomatica())
        );

        verify(facturaService).generarFactura(any(Suscripcion.class));
    }

    @Test
    @DisplayName("Crear suscripción sin renovación automática")
    void crearSuscripcionSinRenovacion() {
        when(suscripcionRepository.save(any(Suscripcion.class)))
            .thenAnswer(inv -> inv.getArgument(0));
        when(facturaService.generarFactura(any(Suscripcion.class)))
            .thenReturn(new Factura());

        Suscripcion resultado = suscripcionService.crearSuscripcion(usuario, plan, false);

        assertFalse(resultado.getRenovacionAutomatica());
    }

    // Tests de activación
    @Test
    @DisplayName("Activar suscripción correctamente")
    void activarSuscripcion() {
        suscripcion.setEstado(Suscripcion.EstadoSuscripcion.CANCELADA);
        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(suscripcion));
        when(suscripcionRepository.save(any(Suscripcion.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        Suscripcion resultado = suscripcionService.activarSuscripcion(1L);

        assertEquals(Suscripcion.EstadoSuscripcion.ACTIVA, resultado.getEstado());
        assertNull(resultado.getFechaFin());
    }

    @Test
    @DisplayName("Activar suscripción inexistente lanza excepción")
    void activarSuscripcionInexistente() {
        when(suscripcionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
            () -> suscripcionService.activarSuscripcion(999L));
    }

    // Tests de cancelación
    @Test
    @DisplayName("Cancelar suscripción correctamente")
    void cancelarSuscripcion() {
        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(suscripcion));
        when(suscripcionRepository.save(any(Suscripcion.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        Suscripcion resultado = suscripcionService.cancelarSuscripcion(1L);

        assertAll(
            () -> assertEquals(Suscripcion.EstadoSuscripcion.CANCELADA, resultado.getEstado()),
            () -> assertNotNull(resultado.getFechaFin()),
            () -> assertFalse(resultado.getRenovacionAutomatica())
        );
    }

    // Tests de impago
    @Test
    @DisplayName("Marcar como impago correctamente")
    void marcarComoImpago() {
        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(suscripcion));
        when(suscripcionRepository.save(any(Suscripcion.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        Suscripcion resultado = suscripcionService.marcarComoImpago(1L);

        assertEquals(Suscripcion.EstadoSuscripcion.IMPAGO, resultado.getEstado());
    }

    // Tests de renovación automática
    @Test
    @DisplayName("Configurar renovación automática")
    void configurarRenovacionAutomatica() {
        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(suscripcion));
        when(suscripcionRepository.save(any(Suscripcion.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        Suscripcion resultado = suscripcionService.configurarRenovacionAutomatica(1L, false);

        assertFalse(resultado.getRenovacionAutomatica());
    }

    // Tests de cambio de plan
    @Test
    @DisplayName("Cambiar plan correctamente")
    void cambiarPlan() {
        Plan nuevoPlan = new Plan();
        nuevoPlan.setId(2L);
        nuevoPlan.setTipo(Plan.TipoPlan.PREMIUM);
        nuevoPlan.setNombre("Plan Premium");
        nuevoPlan.setPrecioMensual(new BigDecimal("19.99"));

        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(suscripcion));
        when(suscripcionRepository.save(any(Suscripcion.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        Suscripcion resultado = suscripcionService.cambiarPlan(1L, nuevoPlan);

        assertEquals(Plan.TipoPlan.PREMIUM, resultado.getPlan().getTipo());
    }

    // Tests de renovación
    @Test
    @DisplayName("Renovar suscripción con renovación automática")
    void renovarSuscripcion() {
        when(facturaService.generarFactura(any(Suscripcion.class)))
            .thenReturn(new Factura());
        when(suscripcionRepository.save(any(Suscripcion.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        LocalDate fechaAnterior = suscripcion.getProximaRenovacion();
        suscripcionService.renovarSuscripcion(suscripcion);

        assertNotEquals(fechaAnterior, suscripcion.getProximaRenovacion());
        verify(facturaService).generarFactura(suscripcion);
    }

    @Test
    @DisplayName("No renovar si renovación automática deshabilitada")
    void noRenovarSinRenovacionAutomatica() {
        suscripcion.setRenovacionAutomatica(false);

        suscripcionService.renovarSuscripcion(suscripcion);

        verify(facturaService, never()).generarFactura(any());
    }

    @Test
    @DisplayName("No renovar si suscripción no está activa")
    void noRenovarSuscripcionInactiva() {
        suscripcion.setEstado(Suscripcion.EstadoSuscripcion.CANCELADA);

        suscripcionService.renovarSuscripcion(suscripcion);

        verify(facturaService, never()).generarFactura(any());
    }

    // Tests de consultas
    @Test
    @DisplayName("Obtener suscripciones de usuario")
    void obtenerSuscripcionesUsuario() {
        when(suscripcionRepository.findByUsuarioId(1L))
            .thenReturn(Arrays.asList(suscripcion));

        List<Suscripcion> resultado = suscripcionService.obtenerSuscripcionesUsuario(1L);

        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("Obtener suscripciones activas de usuario")
    void obtenerSuscripcionesActivas() {
        when(suscripcionRepository.findByUsuarioIdAndEstado(1L, Suscripcion.EstadoSuscripcion.ACTIVA))
            .thenReturn(Arrays.asList(suscripcion));

        List<Suscripcion> resultado = suscripcionService.obtenerSuscripcionesActivas(1L);

        assertEquals(1, resultado.size());
        assertEquals(Suscripcion.EstadoSuscripcion.ACTIVA, resultado.get(0).getEstado());
    }

    @Test
    @DisplayName("Obtener suscripción por ID")
    void obtenerSuscripcion() {
        when(suscripcionRepository.findById(1L)).thenReturn(Optional.of(suscripcion));

        Suscripcion resultado = suscripcionService.obtenerSuscripcion(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    @DisplayName("Obtener suscripción inexistente lanza excepción")
    void obtenerSuscripcionInexistente() {
        when(suscripcionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
            () -> suscripcionService.obtenerSuscripcion(999L));
    }
}

