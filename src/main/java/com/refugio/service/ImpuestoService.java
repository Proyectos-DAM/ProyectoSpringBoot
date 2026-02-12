package com.refugio.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

// Servicio para calcular impuestos según país
@Service
public class ImpuestoService {

    // Tasas de impuesto por país (%)
    private static final Map<String, BigDecimal> TASAS_IMPUESTO = new HashMap<>();

    static {
        // Europa
        TASAS_IMPUESTO.put("ES", new BigDecimal("21.00"));
        TASAS_IMPUESTO.put("DE", new BigDecimal("19.00"));
        TASAS_IMPUESTO.put("FR", new BigDecimal("20.00"));
        TASAS_IMPUESTO.put("IT", new BigDecimal("22.00"));
        TASAS_IMPUESTO.put("PT", new BigDecimal("23.00"));
        TASAS_IMPUESTO.put("UK", new BigDecimal("20.00"));
        TASAS_IMPUESTO.put("NL", new BigDecimal("21.00"));
        TASAS_IMPUESTO.put("BE", new BigDecimal("21.00"));
        TASAS_IMPUESTO.put("AT", new BigDecimal("20.00"));
        TASAS_IMPUESTO.put("CH", new BigDecimal("7.70"));
        TASAS_IMPUESTO.put("SE", new BigDecimal("25.00"));
        TASAS_IMPUESTO.put("NO", new BigDecimal("25.00"));
        TASAS_IMPUESTO.put("DK", new BigDecimal("25.00"));
        TASAS_IMPUESTO.put("FI", new BigDecimal("24.00"));
        TASAS_IMPUESTO.put("PL", new BigDecimal("23.00"));
        TASAS_IMPUESTO.put("IE", new BigDecimal("23.00"));
        TASAS_IMPUESTO.put("GR", new BigDecimal("24.00"));

        // América
        TASAS_IMPUESTO.put("US", new BigDecimal("0.00"));
        TASAS_IMPUESTO.put("MX", new BigDecimal("16.00"));
        TASAS_IMPUESTO.put("AR", new BigDecimal("21.00"));
        TASAS_IMPUESTO.put("BR", new BigDecimal("17.00"));
        TASAS_IMPUESTO.put("CL", new BigDecimal("19.00"));
        TASAS_IMPUESTO.put("CO", new BigDecimal("19.00"));
        TASAS_IMPUESTO.put("PE", new BigDecimal("18.00"));
        TASAS_IMPUESTO.put("UY", new BigDecimal("22.00"));
        TASAS_IMPUESTO.put("VE", new BigDecimal("16.00"));
        TASAS_IMPUESTO.put("EC", new BigDecimal("12.00"));
        TASAS_IMPUESTO.put("CA", new BigDecimal("5.00"));

        // Asia
        TASAS_IMPUESTO.put("JP", new BigDecimal("10.00"));
        TASAS_IMPUESTO.put("CN", new BigDecimal("13.00"));
        TASAS_IMPUESTO.put("IN", new BigDecimal("18.00"));
        TASAS_IMPUESTO.put("KR", new BigDecimal("10.00"));
        TASAS_IMPUESTO.put("SG", new BigDecimal("8.00"));
        TASAS_IMPUESTO.put("HK", new BigDecimal("0.00"));

        // Oceanía
        TASAS_IMPUESTO.put("AU", new BigDecimal("10.00"));
        TASAS_IMPUESTO.put("NZ", new BigDecimal("15.00"));
    }

    private static final BigDecimal TASA_DEFAULT = new BigDecimal("21.00");

    // Obtiene tasa de impuesto para un país
    public BigDecimal obtenerTasaImpuesto(String codigoPais) {
        if (codigoPais == null || codigoPais.isBlank()) {
            return TASA_DEFAULT;
        }
        return TASAS_IMPUESTO.getOrDefault(codigoPais.toUpperCase().trim(), TASA_DEFAULT);
    }

    // Calcula importe del impuesto
    public BigDecimal calcularImpuesto(BigDecimal montoBase, String codigoPais) {
        BigDecimal tasa = obtenerTasaImpuesto(codigoPais);
        return montoBase.multiply(tasa)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    // Calcula total con impuesto
    public BigDecimal calcularTotalConImpuesto(BigDecimal montoBase, String codigoPais) {
        BigDecimal impuesto = calcularImpuesto(montoBase, codigoPais);
        return montoBase.add(impuesto);
    }

    // Calcula detalle completo del impuesto
    public DetalleImpuesto calcularDetalleImpuesto(BigDecimal montoBase, String codigoPais) {
        BigDecimal tasa = obtenerTasaImpuesto(codigoPais);
        BigDecimal impuesto = calcularImpuesto(montoBase, codigoPais);
        BigDecimal total = montoBase.add(impuesto);

        return new DetalleImpuesto(codigoPais, tasa, montoBase, impuesto, total);
    }

    // Verifica si país tiene tasa configurada
    public boolean tieneTasaConfigurada(String codigoPais) {
        return codigoPais != null && TASAS_IMPUESTO.containsKey(codigoPais.toUpperCase().trim());
    }

    // Obtiene todas las tasas
    public Map<String, BigDecimal> obtenerTodasLasTasas() {
        return new HashMap<>(TASAS_IMPUESTO);
    }

    // Clase para detalle de impuesto
    public static class DetalleImpuesto {
        private final String codigoPais;
        private final BigDecimal tasaPorcentaje;
        private final BigDecimal montoBase;
        private final BigDecimal montoImpuesto;
        private final BigDecimal montoTotal;

        public DetalleImpuesto(String codigoPais, BigDecimal tasaPorcentaje,
                              BigDecimal montoBase, BigDecimal montoImpuesto,
                              BigDecimal montoTotal) {
            this.codigoPais = codigoPais;
            this.tasaPorcentaje = tasaPorcentaje;
            this.montoBase = montoBase;
            this.montoImpuesto = montoImpuesto;
            this.montoTotal = montoTotal;
        }

        public String getCodigoPais() { return codigoPais; }
        public BigDecimal getTasaPorcentaje() { return tasaPorcentaje; }
        public BigDecimal getMontoBase() { return montoBase; }
        public BigDecimal getMontoImpuesto() { return montoImpuesto; }
        public BigDecimal getMontoTotal() { return montoTotal; }
    }
}
