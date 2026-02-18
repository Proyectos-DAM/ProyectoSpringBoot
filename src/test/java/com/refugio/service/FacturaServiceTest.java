package com.refugio.service;

import com.refugio.JPA.*;
import com.refugio.repository.FacturaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Test para FacturaService - generación y gestión de facturas
@ExtendWith(MockitoExtension.class)
class FacturaServiceTest {

    @Mock
    private FacturaRepository facturaRepository;

    @Spy
    private ImpuestoService impuestoService = new ImpuestoService();

    @InjectMocks
    private FacturaService facturaService;

    private Usuario usuario;
    private Plan plan;
    private Suscripcion suscripcion;
    private Factura factura;

    @BeforeEach
    void setUp() {
        // Crear usuario con perfil
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("test@test.com");

        Perfil perfil = new Perfil();
        perfil.setPais("ES");
        perfil.setUsuario(usuario);
        usuario.setPerfil(perfil);

        // Crear plan
        plan = new Plan();
        plan.setId(1L);
        plan.setTipo(Plan.TipoPlan.BASIC);
        plan.setNombre("Plan Básico");
        plan.setPrecioMensual(new BigDecimal("100.00"));

        // Crear suscripción
        suscripcion = new Suscripcion();
        suscripcion.setId(1L);
        suscripcion.setUsuario(usuario);
        suscripcion.setPlan(plan);
        suscripcion.setEstado(Suscripcion.EstadoSuscripcion.ACTIVA);

        // Crear factura
        factura = new Factura();
        factura.setId(1L);
        factura.setSuscripcion(suscripcion);
        factura.setFechaEmision(LocalDate.now());
        factura.setImporte(new BigDecimal("100.00"));
        factura.setImporteImpuesto(new BigDecimal("21.00"));
        factura.setImporteTotal(new BigDecimal("121.00"));
        factura.setPaisImpuesto("ES");
        factura.setTasaImpuesto(new BigDecimal("21.00"));
        factura.setEstado(Factura.EstadoFactura.EMITIDA);
    }

    // Tests de generación de factura
    @Test
    @DisplayName("Generar factura con impuestos España")
    void generarFacturaEspana() {
        when(facturaRepository.save(any(Factura.class)))
            .thenAnswer(inv -> {
                Factura f = inv.getArgument(0);
                f.setId(1L);
                return f;
            });

        Factura resultado = facturaService.generarFactura(suscripcion);

        assertAll(
            () -> assertNotNull(resultado),
            () -> assertEquals(new BigDecimal("100.00"), resultado.getImporte()),
            () -> assertEquals(new BigDecimal("21.00"), resultado.getImporteImpuesto()),
            () -> assertEquals(new BigDecimal("121.00"), resultado.getImporteTotal()),
            () -> assertEquals("ES", resultado.getPaisImpuesto()),
            () -> assertEquals(Factura.EstadoFactura.EMITIDA, resultado.getEstado())
        );
    }

    @Test
    @DisplayName("Generar factura con impuestos Alemania")
    void generarFacturaAlemania() {
        usuario.getPerfil().setPais("DE");

        when(facturaRepository.save(any(Factura.class)))
            .thenAnswer(inv -> {
                Factura f = inv.getArgument(0);
                f.setId(1L);
                return f;
            });

        Factura resultado = facturaService.generarFactura(suscripcion);

        assertAll(
            () -> assertEquals(new BigDecimal("19.00"), resultado.getImporteImpuesto()),
            () -> assertEquals(new BigDecimal("119.00"), resultado.getImporteTotal()),
            () -> assertEquals("DE", resultado.getPaisImpuesto())
        );
    }

    @Test
    @DisplayName("Generar factura sin perfil usa país por defecto")
    void generarFacturaSinPerfil() {
        usuario.setPerfil(null);

        when(facturaRepository.save(any(Factura.class)))
            .thenAnswer(inv -> {
                Factura f = inv.getArgument(0);
                f.setId(1L);
                return f;
            });

        Factura resultado = facturaService.generarFactura(suscripcion);

        // Sin perfil usa ES por defecto (21%)
        assertEquals("ES", resultado.getPaisImpuesto());
        assertEquals(new BigDecimal("21.00"), resultado.getImporteImpuesto());
    }

    // Tests de cambio de estado
    @Test
    @DisplayName("Marcar factura como pagada")
    void marcarComoPagada() {
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(factura));
        when(facturaRepository.save(any(Factura.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        Factura resultado = facturaService.marcarComoPagada(1L);

        assertEquals(Factura.EstadoFactura.PAGADA, resultado.getEstado());
    }

    @Test
    @DisplayName("Anular factura")
    void anularFactura() {
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(factura));
        when(facturaRepository.save(any(Factura.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        Factura resultado = facturaService.anularFactura(1L);

        assertEquals(Factura.EstadoFactura.ANULADA, resultado.getEstado());
    }

    @Test
    @DisplayName("Marcar como pagada factura inexistente lanza excepción")
    void marcarComoPagadaInexistente() {
        when(facturaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
            () -> facturaService.marcarComoPagada(999L));
    }

    // Tests de consultas
    @Test
    @DisplayName("Obtener todas las facturas")
    void obtenerTodasLasFacturas() {
        when(facturaRepository.findAllByOrderByFechaEmisionDesc())
            .thenReturn(Arrays.asList(factura));

        List<Factura> resultado = facturaService.obtenerTodasLasFacturas();

        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("Obtener factura por ID")
    void obtenerFactura() {
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(factura));

        Factura resultado = facturaService.obtenerFactura(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    // Tests de filtros
    @Test
    @DisplayName("Filtrar facturas por fecha")
    void filtrarPorFecha() {
        LocalDate inicio = LocalDate.now().minusDays(7);
        LocalDate fin = LocalDate.now();

        when(facturaRepository.findByFechaEmisionBetween(inicio, fin))
            .thenReturn(Arrays.asList(factura));

        List<Factura> resultado = facturaService.filtrarPorFecha(inicio, fin);

        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("Filtrar facturas por monto")
    void filtrarPorMonto() {
        BigDecimal min = new BigDecimal("100.00");
        BigDecimal max = new BigDecimal("200.00");

        when(facturaRepository.findByImporteTotalBetween(min, max))
            .thenReturn(Arrays.asList(factura));

        List<Factura> resultado = facturaService.filtrarPorMonto(min, max);

        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("Obtener facturas por estado")
    void obtenerFacturasPorEstado() {
        when(facturaRepository.findByEstado(Factura.EstadoFactura.EMITIDA))
            .thenReturn(Arrays.asList(factura));

        List<Factura> resultado = facturaService.obtenerFacturasPorEstado(Factura.EstadoFactura.EMITIDA);

        assertEquals(1, resultado.size());
        assertEquals(Factura.EstadoFactura.EMITIDA, resultado.get(0).getEstado());
    }

    // Tests de recálculo de impuesto
    @Test
    @DisplayName("Recalcular impuesto cambiando país")
    void recalcularImpuesto() {
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(factura));
        when(facturaRepository.save(any(Factura.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        // Cambiar de ES (21%) a DE (19%)
        Factura resultado = facturaService.recalcularImpuesto(1L, "DE");

        assertAll(
            () -> assertEquals("DE", resultado.getPaisImpuesto()),
            () -> assertEquals(new BigDecimal("19.00"), resultado.getTasaImpuesto()),
            () -> assertEquals(new BigDecimal("19.00"), resultado.getImporteImpuesto()),
            () -> assertEquals(new BigDecimal("119.00"), resultado.getImporteTotal())
        );
    }

    @Test
    @DisplayName("Recalcular impuesto a USA (0%)")
    void recalcularImpuestoUSA() {
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(factura));
        when(facturaRepository.save(any(Factura.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        Factura resultado = facturaService.recalcularImpuesto(1L, "US");

        assertAll(
            () -> assertEquals("US", resultado.getPaisImpuesto()),
            () -> assertEquals(new BigDecimal("0.00"), resultado.getImporteImpuesto()),
            () -> assertEquals(new BigDecimal("100.00"), resultado.getImporteTotal())
        );
    }
}

