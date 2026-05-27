package com.aureus.controller;

import com.aureus.dto.solicitud.SolicitudEntradaDto;
import com.aureus.dto.solicitud.SolicitudSalidaDto;
import com.aureus.model.Solicitud.TipoSolicitud;
import com.aureus.service.SolicitudService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Módulo 5 — pruebas de seguridad con @SpringBootTest y @WithMockUser.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SolicitudSecurityTest {

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper mapper;

    @MockBean SolicitudService svc;

    /** Prueba 1: sin token → debe bloquear antes de llegar al servicio */
    @Test
    void sinToken_bloqueado() throws Exception {
        mockMvc.perform(post("/api/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(entrada())))
                .andExpect(status().is(
                        org.hamcrest.Matchers.anyOf(
                                org.hamcrest.Matchers.is(401),
                                org.hamcrest.Matchers.is(403)
                        )
                ));
    }

    /** Prueba 2: ROLE_USER puede abrir solicitudes → 201 */
    @Test
    @WithMockUser(username = "pedro@aureus.com", roles = "USER")
    void conRolUser_crea201() throws Exception {
        SolicitudSalidaDto ficticio = mock(SolicitudSalidaDto.class);
        when(ficticio.getId()).thenReturn(5L);
        when(ficticio.getEstado()).thenReturn("PENDIENTE");
        when(svc.abrir(anyString(), any())).thenReturn(ficticio);

        mockMvc.perform(post("/api/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(entrada())))
                .andExpect(status().isCreated());
    }

    /** Prueba 3: ROLE_USER intentando aprobar → 403 sin tocar la lógica */
    @Test
    @WithMockUser(username = "pedro@aureus.com", roles = "USER")
    void sinAdmin_noPuedeAprobar() throws Exception {
        mockMvc.perform(put("/api/solicitudes/42/aprobar")
                        .param("observacion", "Prueba de escalada de privilegios"))
                .andExpect(status().isForbidden());
    }

    private SolicitudEntradaDto entrada() {
        SolicitudEntradaDto dto = new SolicitudEntradaDto();
        dto.setTipo(TipoSolicitud.INFORMACION);
        dto.setContenido("Requiero información sobre el proceso de matrícula.");
        return dto;
    }
}
