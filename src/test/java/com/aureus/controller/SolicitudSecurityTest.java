package com.aureus.controller;

import com.aureus.dto.solicitud.CrearSolicitudDto;
import com.aureus.dto.solicitud.SolicitudDto;
import com.aureus.model.Solicitud.TipoSolicitud;
import com.aureus.service.SolicitudService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Módulo 5 — Pruebas de Seguridad con @WithMockUser.
 *
 * Levanta el contexto completo de Spring con @SpringBootTest.
 * BD apuntada al perfil "test" (H2 en memoria).
 * Usuarios simulados con @WithMockUser para verificar reglas de seguridad.
 *
 * Pruebas requeridas:
 *  1. POST /api/solicitudes sin autenticación           → HTTP 401 o 403
 *  2. POST /api/solicitudes con usuario autenticado     → HTTP 201 Created
 *  3. PUT  /api/solicitudes/{id}/aprobar con rol USER   → HTTP 403 Forbidden
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("SolicitudSecurityTest — @WithMockUser")
class SolicitudSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /** Mockeamos el servicio para no necesitar datos reales en BD. */
    @MockBean
    private SolicitudService solicitudService;

    // ── Prueba 1: POST sin autenticación → 401 o 403 ─────────────────────

    @Test
    @DisplayName("1. POST /api/solicitudes sin autenticación → HTTP 401 o 403")
    void crear_sinAutenticacion_retorna401o403() throws Exception {
        String body = objectMapper.writeValueAsString(bodyValido());

        mockMvc.perform(post("/api/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is(
                        org.hamcrest.Matchers.anyOf(
                                org.hamcrest.Matchers.is(401),
                                org.hamcrest.Matchers.is(403)
                        )
                ));
    }

    // ── Prueba 2: POST con usuario autenticado (sin rol especial) → 201 ──

    @Test
    @DisplayName("2. POST /api/solicitudes con usuario autenticado sin rol especial → HTTP 201 Created")
    @WithMockUser(username = "user@aureus.com", roles = "USER")
    void crear_usuarioAutenticado_retorna201() throws Exception {
        // Stub: el servicio devuelve un DTO ficticio
        when(solicitudService.crear(eq("user@aureus.com"), any()))
                .thenReturn(solicitudDtoFicticia());

        String body = objectMapper.writeValueAsString(bodyValido());

        mockMvc.perform(post("/api/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    // ── Prueba 3: PUT aprobar con rol USER (sin ADMIN) → 403 ─────────────

    @Test
    @DisplayName("3. PUT /api/solicitudes/{id}/aprobar con rol USER (sin ADMIN) → HTTP 403 Forbidden")
    @WithMockUser(username = "user@aureus.com", roles = "USER")
    void aprobar_sinRolAdmin_retorna403() throws Exception {
        // El ID puede no existir — el sistema debe rechazar en seguridad antes de buscar en BD
        mockMvc.perform(put("/api/solicitudes/999/aprobar")
                        .param("observacion", "Intento no autorizado"))
                .andExpect(status().isForbidden());
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private CrearSolicitudDto bodyValido() {
        CrearSolicitudDto dto = new CrearSolicitudDto();
        dto.setTipo(TipoSolicitud.SOPORTE);
        dto.setDescripcion("Necesito soporte técnico con mi cuenta.");
        return dto;
    }

    private SolicitudDto solicitudDtoFicticia() {
        // Usamos reflexión-libre: creamos el DTO a través del constructor normal del modelo ficticio
        // En lugar de instanciar directamente (constructor privado), retornamos un mock de Mockito
        SolicitudDto dto = mock(SolicitudDto.class);
        when(dto.getId()).thenReturn(1L);
        when(dto.getEstado()).thenReturn("PENDIENTE");
        when(dto.getTipo()).thenReturn("SOPORTE");
        when(dto.getDescripcion()).thenReturn("Necesito soporte técnico con mi cuenta.");
        when(dto.getSolicitanteEmail()).thenReturn("user@aureus.com");
        when(dto.getSolicitanteNombre()).thenReturn("Usuario Test");
        when(dto.getFechaCreacion()).thenReturn(LocalDateTime.now());
        return dto;
    }
}
