package com.aureus.controller;

import com.aureus.security.AureusUserDetailsService;
import com.aureus.security.JwtAuthFilter;
import com.aureus.service.MensajeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Módulo 4 — @WebMvcTest sobre MensajeController.
 * JwtAuthFilter y AureusUserDetailsService se mockean porque SecurityConfig los requiere.
 */
@WebMvcTest(MensajeController.class)
class MensajeControllerTest {

    @Autowired MockMvc       mockMvc;
    @Autowired ObjectMapper  mapper;

    @MockBean JwtAuthFilter            jwtAuthFilter;
    @MockBean AureusUserDetailsService aureusUserDetailsService;
    @MockBean MensajeService           mensajeService;

    /** Prueba 1: autenticado → 200 y lista vacía */
    @Test
    @WithMockUser(username = "juan@aureus.com", roles = "USER")
    void bandejaAutenticado_retorna200() throws Exception {
        when(mensajeService.recibidos(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/api/mensajes/bandeja-entrada"))
                .andExpect(status().isOk());
    }

    /** Prueba 2: sin sesión → 401 o 403 */
    @Test
    void bandejaSinSesion_retornaNoAutorizado() throws Exception {
        mockMvc.perform(get("/api/mensajes/bandeja-entrada"))
                .andExpect(status().is(
                        org.hamcrest.Matchers.anyOf(
                                org.hamcrest.Matchers.is(401),
                                org.hamcrest.Matchers.is(403)
                        )
                ));
    }

    /** Prueba 3: POST con body vacío → 400 por validaciones @NotBlank */
    @Test
    @WithMockUser(username = "juan@aureus.com", roles = "USER")
    void postCuerpoVacio_retorna400() throws Exception {
        mockMvc.perform(post("/api/mensajes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
