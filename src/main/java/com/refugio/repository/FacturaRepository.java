package com.refugio.repository;

import com.refugio.JPA.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Repositorio de facturas
@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {

    List<Factura> findByFechaEmisionBetween(LocalDate fechaInicio, LocalDate fechaFin);

    List<Factura> findByImporteTotalGreaterThanEqual(BigDecimal montoMinimo);

    List<Factura> findByImporteTotalLessThanEqual(BigDecimal montoMaximo);

    List<Factura> findByImporteTotalBetween(BigDecimal montoMinimo, BigDecimal montoMaximo);

    List<Factura> findByEstado(Factura.EstadoFactura estado);

    @Query("SELECT f FROM Factura f WHERE f.suscripcion.usuario.id = :usuarioId")
    List<Factura> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("SELECT f FROM Factura f WHERE f.fechaEmision BETWEEN :fechaInicio AND :fechaFin " +
            "AND f.importeTotal BETWEEN :montoMinimo AND :montoMaximo")
    List<Factura> findByFechaAndMonto(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("montoMinimo") BigDecimal montoMinimo,
            @Param("montoMaximo") BigDecimal montoMaximo);

    // Query con filtros opcionales
    @Query("SELECT f FROM Factura f WHERE " +
            "(:fechaInicio IS NULL OR f.fechaEmision >= :fechaInicio) AND " +
            "(:fechaFin IS NULL OR f.fechaEmision <= :fechaFin) AND " +
            "(:montoMinimo IS NULL OR f.importeTotal >= :montoMinimo) AND " +
            "(:montoMaximo IS NULL OR f.importeTotal <= :montoMaximo) AND " +
            "(:estado IS NULL OR f.estado = :estado)")
    List<Factura> findByFiltros(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("montoMinimo") BigDecimal montoMinimo,
            @Param("montoMaximo") BigDecimal montoMaximo,
            @Param("estado") Factura.EstadoFactura estado);

    List<Factura> findAllByOrderByFechaEmisionDesc();

    List<Factura> findByEstadoOrderByFechaEmisionAsc(Factura.EstadoFactura estado);
}
