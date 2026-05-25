package com.aureus.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración global de Swagger / OpenAPI 3 (U11 §7.1).
 *
 * springdoc-openapi genera automáticamente la documentación combinando esta
 * metadata global con las anotaciones de controladores y DTOs.
 *
 * Rutas de acceso (application.properties):
 *   Swagger UI: /swagger-ui.html
 *   JSON spec:  /api-docs
 *
 * Esquema JWT Bearer: permite probar endpoints protegidos desde Swagger UI.
 * Flujo: POST /api/auth/login → copiar token → botón "Authorize" → pegar.
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title       = "Aureus API — Finanzas Personales",
        version     = "2.0.0",
        description = "API REST para el simulador de presupuesto personal Aureus. "
                    + "Gestiona transacciones, presupuestos y metas de ahorro. "
                    + "Endpoints protegidos requieren JWT Bearer. "
                    + "Obtener token con POST /api/auth/login → botón Authorize.",
        contact = @Contact(name = "Equipo Aureus", email = "soporte@aureus.app"),
        license = @License(name = "MIT", url = "https://opensource.org/licenses/MIT")
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Desarrollo"),
        @Server(url = "https://aureus.app",    description = "Producción")
    },
    security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
    name         = "bearerAuth",
    type         = SecuritySchemeType.HTTP,
    scheme       = "bearer",
    bearerFormat = "JWT",
    description  = "Token JWT de POST /api/auth/login. Formato: Bearer <token>"
)
public class OpenApiConfig {
    // springdoc escanea el classpath — no se necesitan métodos adicionales.
}
