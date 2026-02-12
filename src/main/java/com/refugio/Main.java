package com.refugio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// Aplicación SpringBoot
@SpringBootApplication
@EnableScheduling
public class Main {
    public static void main(String[] args) {
        System.out.println(">>> INICIANDO APLICACIÓN SPRING BOOT...");
        SpringApplication.run(Main.class, args);
        System.out.println("==============================================");
        System.out.println("  Sistema de Gestión de Suscripciones");
        System.out.println("  SERVIDOR CORRIENDO EN: http://localhost:8080");
        System.out.println("==============================================");
        System.out.println("  Rutas disponibles:");
        System.out.println("  - /facturacion          - Vista de Facturación");
        System.out.println("  - /suscripciones        - Gestión de Suscripciones");
        System.out.println("  - /admin/auditoria      - Panel de Auditoría");
        System.out.println("  - /h2-console           - Consola H2 Database");
        System.out.println("==============================================");
    }
}