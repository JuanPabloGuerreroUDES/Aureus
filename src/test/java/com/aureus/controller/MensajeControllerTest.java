package com.aureus.controller;

import com.aureus.security.AureusUserDetailsService;
import com.aureus.security.JwtAuthFilter;
import com.aureus.service.MensajeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Módulo 4 — Pruebas de Controlador con @WebMvcTest.
 *
 * Carga únicamente la capa web (sin contexto completo ni BD).
 * El servicio se mockea con @MockBean para aislar el controlador.
 *
 * IMPORTANTE: @WebMvcTest carga SecurityConfig, que depende de JwtAuthFilter
 * y AureusUserDetailsService. Ambos deben mockearse con @MockBean para que
 * el contexto de prueba arranque correctamente sin tocar la BD.
 *
 * Pruebas requeridas:
 *  1. GET /api/mensajes/bandeja-entrada con usuario autenticado → HTTP 200
 *  2. GET /api/mensajes/bandeja-entrada sin autenticación       → HTTP 401 o 403
 *  3. POST /api/mensajes con cuerpo vacío                       → HTTP 400
 */
@WebMvcTest(MensajeController.class)
@DisplayName("MensajeControllerTest — @WebMvcTest")
class MensajeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /** Requerido por SecurityConfig — evita que busque BD al cargar contexto. */
    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    /** Requerido por SecurityConfig — evita que busque BD al cargar contexto. */
    @MockBean
    private AureusUserDetailsService aureusUserDetailsService;

    /** Mock del servicio: no toca la BD. */
    @MockBean
    private MensajeService mensajeService;

    // ── Prueba 1: GET bandeja-entrada con usuario autenticado → 200 ──────

    @Test
    @DisplayName("1. GET /api/mensajes/bandeja-entrada con usuario autenticado → HTTP 200 OK")
    @WithMockUser(username = "test@aureus.com", roles = "USER")
    void bandejaEntrada_usuarioAutenticado_retorna200() throws Exception {
        when(mensajeService.bandeja(eq("test@aureus.com")))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/mensajes/bandeja-entrada"))
                .andExpect(status().isOk());
    }

    // ── Prueba 2: GET bandeja-entrada SIN autenticación → 401 o 403 ──────

    @Test
    @DisplayName("2. GET /api/mensajes/bandeja-entrada sin autenticación → HTTP 401 o 403")
    void bandejaEntrada_sinAutenticacion_retorna401o403() throws Exception {
        mockMvc.perform(get("/api/mensajes/bandeja-entrada"))
                .andExpect(status().is(
                        org.hamcrest.Matchers.anyOf(
                                org.hamcrest.Matchers.is(401),
                                org.hamcrest.Matchers.is(403)
                        )
                ));
    }

    // ── Prueba 3: POST /api/mensajes con cuerpo vacío → 400 ──────────────

    @Test
    @DisplayName("3. POST /api/mensajes con cuerpo vacío → HTTP 400 Bad Request")
    @WithMockUser(username = "test@aureus.com", roles = "USER")
    void enviarMensaje_cuerpoVacio_retorna400() throws Exception {
        mockMvc.perform(post("/api/mensajes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
