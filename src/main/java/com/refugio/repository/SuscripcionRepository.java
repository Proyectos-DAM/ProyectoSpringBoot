package com.refugio.repository;

import com.refugio.JPA.Suscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

// Repositorio de suscripciones
@Repository
public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {

    List<Suscripcion> findByUsuarioIdAndEstado(Long usuarioId, Suscripcion.EstadoSuscripcion estado);

    @Query("SELECT s FROM Suscripcion s WHERE s.renovacionAutomatica = true " +
            "AND s.estado = 'ACTIVA' " +
            "AND s.proximaRenovacion <= :fecha")
    List<Suscripcion> findSuscripcionesParaRenovar(@Param("fecha") LocalDate fecha);

    @Query("SELECT s FROM Suscripcion s WHERE s.estado = 'ACTIVA' " +
            "AND s.proximaRenovacion BETWEEN :fechaInicio AND :fechaFin")
    List<Suscripcion> findSuscripcionesProximasAVencer(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);

    List<Suscripcion> findByEstado(Suscripcion.EstadoSuscripcion estado);

    @Query("SELECT s FROM Suscripcion s WHERE s.renovacionAutomatica = false " +
            "AND s.estado = 'ACTIVA' " +
            "AND s.fechaFin <= :fecha")
    List<Suscripcion> findSuscripcionesExpiradas(@Param("fecha") LocalDate fecha);

    List<Suscripcion> findByUsuarioId(Long usuarioId);
}
