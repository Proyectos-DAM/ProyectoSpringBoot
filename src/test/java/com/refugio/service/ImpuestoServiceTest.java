package com.refugio.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

// Test para ImpuestoService - cálculo de impuestos por país
class ImpuestoServiceTest {

    private ImpuestoService impuestoService;

    @BeforeEach
    void setUp() {
        impuestoService = new ImpuestoService();
    }

    // Tests de obtención de tasas
    @Test
    @DisplayName("Tasa España = 21%")
    void obtenerTasaEspana() {
        BigDecimal tasa = impuestoService.obtenerTasaImpuesto("ES");
        assertEquals(new BigDecimal("21.00"), tasa);
    }

    @Test
    @DisplayName("Tasa Alemania = 19%")
    void obtenerTasaAlemania() {
        BigDecimal tasa = impuestoService.obtenerTasaImpuesto("DE");
        assertEquals(new BigDecimal("19.00"), tasa);
    }

    @Test
    @DisplayName("Tasa USA = 0%")
    void obtenerTasaUSA() {
        BigDecimal tasa = impuestoService.obtenerTasaImpuesto("US");
        assertEquals(new BigDecimal("0.00"), tasa);
    }

    @Test
    @DisplayName("País desconocido usa tasa por defecto 21%")
    void obtenerTasaPaisDesconocido() {
        BigDecimal tasa = impuestoService.obtenerTasaImpuesto("XX");
        assertEquals(new BigDecimal("21.00"), tasa);
    }

    @Test
    @DisplayName("País null usa tasa por defecto")
    void obtenerTasaPaisNull() {
        BigDecimal tasa = impuestoService.obtenerTasaImpuesto(null);
        assertEquals(new BigDecimal("21.00"), tasa);
    }

    @Test
    @DisplayName("País vacío usa tasa por defecto")
    void obtenerTasaPaisVacio() {
        BigDecimal tasa = impuestoService.obtenerTasaImpuesto("");
        assertEquals(new BigDecimal("21.00"), tasa);
    }

    // Tests de cálculo de impuesto
    @Test
    @DisplayName("Calcular impuesto 100€ en España = 21€")
    void calcularImpuestoEspana() {
        BigDecimal impuesto = impuestoService.calcularImpuesto(new BigDecimal("100.00"), "ES");
        assertEquals(new BigDecimal("21.00"), impuesto);
    }

    @Test
    @DisplayName("Calcular impuesto 50€ en Alemania = 9.50€")
    void calcularImpuestoAlemania() {
        BigDecimal impuesto = impuestoService.calcularImpuesto(new BigDecimal("50.00"), "DE");
        assertEquals(new BigDecimal("9.50"), impuesto);
    }

    @Test
    @DisplayName("Calcular impuesto en USA = 0€")
    void calcularImpuestoUSA() {
        BigDecimal impuesto = impuestoService.calcularImpuesto(new BigDecimal("100.00"), "US");
        assertEquals(new BigDecimal("0.00"), impuesto);
    }

    // Tests de total con impuesto
    @Test
    @DisplayName("Total 100€ + IVA España = 121€")
    void calcularTotalConImpuestoEspana() {
        BigDecimal total = impuestoService.calcularTotalConImpuesto(new BigDecimal("100.00"), "ES");
        assertEquals(new BigDecimal("121.00"), total);
    }

    @Test
    @DisplayName("Total 100€ + IVA USA = 100€")
    void calcularTotalConImpuestoUSA() {
        BigDecimal total = impuestoService.calcularTotalConImpuesto(new BigDecimal("100.00"), "US");
        assertEquals(new BigDecimal("100.00"), total);
    }

    // Tests de detalle de impuesto
    @Test
    @DisplayName("Detalle impuesto contiene datos correctos")
    void calcularDetalleImpuesto() {
        ImpuestoService.DetalleImpuesto detalle =
            impuestoService.calcularDetalleImpuesto(new BigDecimal("100.00"), "ES");

        assertAll(
            () -> assertEquals("ES", detalle.getCodigoPais()),
            () -> assertEquals(new BigDecimal("21.00"), detalle.getTasaPorcentaje()),
            () -> assertEquals(new BigDecimal("100.00"), detalle.getMontoBase()),
            () -> assertEquals(new BigDecimal("21.00"), detalle.getMontoImpuesto()),
            () -> assertEquals(new BigDecimal("121.00"), detalle.getMontoTotal())
        );
    }

    // Tests de verificación de tasa configurada
    @Test
    @DisplayName("España tiene tasa configurada")
    void tieneTasaConfiguradaEspana() {
        assertTrue(impuestoService.tieneTasaConfigurada("ES"));
    }

    @Test
    @DisplayName("País desconocido no tiene tasa configurada")
    void noTieneTasaConfiguradaPaisDesconocido() {
        assertFalse(impuestoService.tieneTasaConfigurada("XX"));
    }

    @Test
    @DisplayName("Null no tiene tasa configurada")
    void noTieneTasaConfiguradaNull() {
        assertFalse(impuestoService.tieneTasaConfigurada(null));
    }

    // Test redondeo
    @Test
    @DisplayName("Redondeo correcto con decimales")
    void redondeoDecimales() {
        // 33.33€ al 21% = 6.9993 -> 7.00€ (redondeo HALF_UP)
        BigDecimal impuesto = impuestoService.calcularImpuesto(new BigDecimal("33.33"), "ES");
        assertEquals(new BigDecimal("7.00"), impuesto);
    }
}

