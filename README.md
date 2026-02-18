# RefugioApp - Sistema de Suscripciones

Proyecto para la asignatura de Acceso a Datos (DAM).

Sistema de gestión de suscripciones con facturación, cálculo de impuestos por país y auditoría.

## Tecnologías

- Java 21
- Spring Boot 3.2.2
- Spring Data JPA
- Hibernate Envers
- H2 Database
- Thymeleaf
- Maven

## Estructura

```
src/main/java/com/refugio/
├── JPA/           # Entidades (Usuario, Suscripcion, Factura, Plan...)
├── repository/    # Repositorios JPA
├── service/       # Lógica de negocio
├── controller/    # Controladores web
└── config/        # Configuración
```

## Funcionalidades

- Gestión de suscripciones (crear, activar, cancelar, renovar)
- Facturación automática con impuestos por país
- Filtros de facturas por fecha, monto y estado
- Auditoría de cambios con Hibernate Envers
- Dashboard con estadísticas

## Cómo ejecutar

1. Abrir en IntelliJ IDEA
2. Ejecutar `Main.java`
3. Ir a http://localhost:8080

## URLs

| Ruta | Descripción |
|------|-------------|
| `/` | Dashboard |
| `/suscripciones` | Gestión de suscripciones |
| `/facturacion` | Panel de facturas |
| `/facturacion/estadisticas` | Estadísticas |
| `/admin/auditoria` | Panel de auditoría |
| `/h2-console` | Consola BD (user: sa, pass: vacía) |

## Diagrama E-R

```
USUARIO 1──1 PERFIL
   │
   1
   │
   N
SUSCRIPCION N──1 PLAN
   │
   1
   │
   N
FACTURA
```

**Relaciones:**
- Usuario tiene un Perfil (1:1)
- Usuario tiene muchas Suscripciones (1:N)
- Suscripcion pertenece a un Plan (N:1)
- Suscripcion tiene muchas Facturas (1:N)

## Tests

Tests unitarios con JUnit 5 y Mockito en `src/test/java/com/refugio/service/`

Ejecutar: `mvn test`

## Tabla de pruebas

### ImpuestoServiceTest

| Caso de prueba | Resultado | Corrección |
|----------------|-----------|------------|
| Tasa España = 21% | OK | - |
| Tasa Alemania = 19% | OK | - |
| Tasa USA = 0% | OK | - |
| País desconocido usa tasa defecto | OK | - |
| País null usa tasa defecto | OK | - |
| País vacío usa tasa defecto | OK | - |
| Impuesto 100€ España = 21€ | OK | - |
| Impuesto 50€ Alemania = 9.50€ | OK | - |
| Impuesto en USA = 0€ | OK | - |
| Total con IVA España | OK | - |
| Total con IVA USA | OK | - |
| Detalle impuesto correcto | OK | - |
| Redondeo decimales | OK | - |

### SuscripcionServiceTest

| Caso de prueba | Resultado | Corrección |
|----------------|-----------|------------|
| Crear suscripción | OK | - |
| Crear sin renovación auto | OK | - |
| Activar suscripción | OK | - |
| Activar inexistente lanza error | OK | - |
| Cancelar suscripción | OK | - |
| Marcar como impago | OK | - |
| Configurar renovación auto | OK | - |
| Cambiar plan | OK | - |
| Renovar suscripción | OK | - |
| No renovar si auto deshabilitada | OK | - |
| No renovar si no activa | OK | - |
| Obtener suscripciones usuario | OK | - |
| Obtener activas usuario | OK | - |
| Obtener por ID | OK | - |
| Obtener inexistente lanza error | OK | - |

### FacturaServiceTest

| Caso de prueba | Resultado | Corrección |
|----------------|-----------|------------|
| Generar factura España | OK | - |
| Generar factura Alemania | OK | - |
| Factura sin perfil usa defecto | OK | - |
| Marcar como pagada | OK | - |
| Anular factura | OK | - |
| Pagar inexistente lanza error | OK | - |
| Obtener todas | OK | - |
| Obtener por ID | OK | - |
| Filtrar por fecha | OK | - |
| Filtrar por monto | OK | - |
| Obtener por estado | OK | - |
| Recalcular impuesto | OK | - |

### Resumen

| Servicio | Tests | Pasados |
|----------|-------|---------|
| ImpuestoService | 13 | 13 |
| SuscripcionService | 15 | 15 |
| FacturaService | 12 | 12 |
| **Total** | **40** | **40** |

## Versión

V3 - Semana 3 (Final)

**Cambios principales en V3:**
- ✅ Tests JUnit (40 casos de prueba documentados)
- ✅ Mejoras UI/UX (Dashboard, navegación mejorada)
- ✅ README completo con documentación
- ✅ Tabla de pruebas y diagrama E-R

---
Acceso a Datos - DAM 2025/2026
