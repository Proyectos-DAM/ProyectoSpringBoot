package com.refugio.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

// Servicio de auditoría con Hibernate Envers
@Service
public class AuditoriaService {

    @PersistenceContext
    private EntityManager entityManager;

    // Obtiene historial de una entidad
    @Transactional(readOnly = true)
    public <T> List<RegistroAuditoria<T>> obtenerHistorial(Class<T> claseEntidad, Long entityId) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        List<Number> revisiones = auditReader.getRevisions(claseEntidad, entityId);
        List<RegistroAuditoria<T>> historial = new ArrayList<>();

        for (Number revision : revisiones) {
            T entidad = auditReader.find(claseEntidad, entityId, revision);
            Date fechaRevision = auditReader.getRevisionDate(revision);

            historial.add(new RegistroAuditoria<>(
                    revision.longValue(),
                    fechaRevision,
                    entidad,
                    obtenerTipoRevision(claseEntidad, entityId, revision, auditReader)
            ));
        }

        return historial;
    }

    // Obtiene tipo de revisión
    private <T> String obtenerTipoRevision(Class<T> claseEntidad, Long entityId,
                                            Number revision, AuditReader auditReader) {
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> resultados = auditReader.createQuery()
                    .forRevisionsOfEntity(claseEntidad, false, true)
                    .add(AuditEntity.id().eq(entityId))
                    .add(AuditEntity.revisionNumber().eq(revision))
                    .getResultList();

            if (!resultados.isEmpty()) {
                Object[] resultado = resultados.get(0);
                RevisionType tipo = (RevisionType) resultado[2];
                return switch (tipo) {
                    case ADD -> "CREACIÓN";
                    case MOD -> "MODIFICACIÓN";
                    case DEL -> "ELIMINACIÓN";
                };
            }
        } catch (Exception e) {
            // Error ignorado
        }
        return "DESCONOCIDO";
    }

    // Obtiene cambios recientes de todas las entidades
    @Transactional(readOnly = true)
    public List<ResumenCambio> obtenerCambiosRecientes(int maxResultados) {
        List<ResumenCambio> cambios = new ArrayList<>();
        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        agregarCambiosDeEntidad(auditReader, com.refugio.JPA.Usuario.class, "Usuario", cambios, maxResultados);
        agregarCambiosDeEntidad(auditReader, com.refugio.JPA.Suscripcion.class, "Suscripción", cambios, maxResultados);

        cambios.sort((a, b) -> b.getFecha().compareTo(a.getFecha()));

        if (cambios.size() > maxResultados) {
            return cambios.subList(0, maxResultados);
        }

        return cambios;
    }

    // Agrega cambios de una entidad a la lista
    @SuppressWarnings("unchecked")
    private <T> void agregarCambiosDeEntidad(AuditReader auditReader, Class<T> claseEntidad,
                                              String nombreEntidad, List<ResumenCambio> cambios,
                                              int maxResultados) {
        try {
            AuditQuery query = auditReader.createQuery()
                    .forRevisionsOfEntity(claseEntidad, false, true)
                    .addOrder(AuditEntity.revisionNumber().desc())
                    .setMaxResults(maxResultados);

            List<Object[]> resultados = query.getResultList();

            for (Object[] resultado : resultados) {
                Object entidad = resultado[0];
                Number revision = (Number) ((org.hibernate.envers.DefaultRevisionEntity) resultado[1]).getId();
                Date fecha = ((org.hibernate.envers.DefaultRevisionEntity) resultado[1]).getRevisionDate();
                RevisionType tipo = (RevisionType) resultado[2];

                String tipoAccion = switch (tipo) {
                    case ADD -> "CREACIÓN";
                    case MOD -> "MODIFICACIÓN";
                    case DEL -> "ELIMINACIÓN";
                };

                String descripcion = generarDescripcion(entidad, nombreEntidad);
                Long entityId = obtenerIdEntidad(entidad);

                cambios.add(new ResumenCambio(
                        revision.longValue(),
                        fecha,
                        nombreEntidad,
                        entityId,
                        tipoAccion,
                        descripcion
                ));
            }
        } catch (Exception e) {
            // Error ignorado
        }
    }

    // Genera descripción del cambio
    private String generarDescripcion(Object entidad, String nombreEntidad) {
        if (entidad instanceof com.refugio.JPA.Usuario usuario) {
            return "Usuario: " + usuario.getEmail();
        } else if (entidad instanceof com.refugio.JPA.Suscripcion suscripcion) {
            return "Suscripción ID: " + suscripcion.getId() + " - Estado: " + suscripcion.getEstado();
        }
        return nombreEntidad + " modificado";
    }

    // Obtiene ID de la entidad
    private Long obtenerIdEntidad(Object entidad) {
        if (entidad instanceof com.refugio.JPA.Usuario usuario) {
            return usuario.getId();
        } else if (entidad instanceof com.refugio.JPA.Suscripcion suscripcion) {
            return suscripcion.getId();
        }
        return null;
    }

    // Obtiene versión anterior de una entidad
    @Transactional(readOnly = true)
    public <T> T obtenerVersionAnterior(Class<T> claseEntidad, Long entityId, Long numeroRevision) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        return auditReader.find(claseEntidad, entityId, numeroRevision);
    }

    // Clase para registro de auditoría
    public static class RegistroAuditoria<T> {
        private final Long numeroRevision;
        private final Date fecha;
        private final T entidad;
        private final String tipoAccion;

        public RegistroAuditoria(Long numeroRevision, Date fecha, T entidad, String tipoAccion) {
            this.numeroRevision = numeroRevision;
            this.fecha = fecha;
            this.entidad = entidad;
            this.tipoAccion = tipoAccion;
        }

        public Long getNumeroRevision() { return numeroRevision; }
        public Date getFecha() { return fecha; }
        public T getEntidad() { return entidad; }
        public String getTipoAccion() { return tipoAccion; }
    }

    // Clase para resumen de cambio
    public static class ResumenCambio {
        private final Long numeroRevision;
        private final Date fecha;
        private final String tipoEntidad;
        private final Long entityId;
        private final String tipoAccion;
        private final String descripcion;

        public ResumenCambio(Long numeroRevision, Date fecha, String tipoEntidad,
                            Long entityId, String tipoAccion, String descripcion) {
            this.numeroRevision = numeroRevision;
            this.fecha = fecha;
            this.tipoEntidad = tipoEntidad;
            this.entityId = entityId;
            this.tipoAccion = tipoAccion;
            this.descripcion = descripcion;
        }

        public Long getNumeroRevision() { return numeroRevision; }
        public Date getFecha() { return fecha; }
        public String getTipoEntidad() { return tipoEntidad; }
        public Long getEntityId() { return entityId; }
        public String getTipoAccion() { return tipoAccion; }
        public String getDescripcion() { return descripcion; }
    }
}

