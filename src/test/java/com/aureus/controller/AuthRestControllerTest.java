package com.aureus.controller;

import com.aureus.model.User;
import com.aureus.security.AureusUserDetailsService;
import com.aureus.security.JwtAuthFilter;
import com.aureus.security.JwtUtils;
import com.aureus.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de la capa web REST con @WebMvcTest (U10 §4.2).
 *
 * @WebMvcTest carga ÚNICAMENTE la capa web:
 *   - Controllers, filtros, message converters, MVC config.
 *   - NO carga JPA, servicios reales, seguridad completa.
 *
 * excludeAutoConfiguration = SecurityAutoConfiguration.class:
 *   Desactiva Spring Security para este test, permitiendo enfocarse
 *   en la lógica del controlador sin conflictos de autenticación.
 *   En un proyecto real, se usaría @WithMockUser para tests con seguridad.
 *
 * @MockBean reemplaza las dependencias del controlador con mocks de Mockito,
 * idéntico a @Mock pero integrado en el contexto de Spring (U10 §3.2).
 */
@WebMvcTest(
    controllers = AuthRestController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@DisplayName("AuthRestController — capa web REST")
class AuthRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // @MockBean integra mocks en el contexto Spring (U10 §3.2)
    @MockBean private AuthenticationManager    authenticationManager;
    @MockBean private JwtUtils                 jwtUtils;
    @MockBean private UserService              userService;
    @MockBean private JwtAuthFilter            jwtAuthFilter;          // evitar autowire en context
    @MockBean private AureusUserDetailsService userDetailsService;     // idem

    // ── POST /api/auth/login ──────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {

        @Test
        @DisplayName("con credenciales válidas retorna 200 con token JWT")
        void login_credencialesValidas_retorna200ConToken() throws Exception {
            // Given
            User usuario = new User("Juan", "juan@test.com", "$2a$12$hash");

            UserDetails userDetails = org.springframework.security.core.userdetails.User
                    .withUsername("juan@test.com")
                    .password("$2a$12$hash")
                    .authorities(Collections.emptyList())
                    .build();

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, Collections.emptyList());

            when(authenticationManager.authenticate(any())).thenReturn(auth);
            when(userService.buscarPorEmail("juan@test.com")).thenReturn(usuario);
            when(jwtUtils.generateToken("juan@test.com", null)).thenReturn("eyJ.test.token");

            Map<String, String> body = Map.of("email", "juan@test.com", "password", "Segura123!");

            // When / Then
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("eyJ.test.token"))
                    .andExpect(jsonPath("$.email").value("juan@test.com"))
                    .andExpect(jsonPath("$.tipo").value("Bearer"));
        }

        @Test
        @DisplayName("con credenciales incorrectas retorna 401 sin revelar detalles")
        void login_credencialesInvalidas_retorna401ConMensajeGenerico() throws Exception {
            // Given — AuthenticationManager lanza BadCredentialsException
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            Map<String, String> body = Map.of("email", "malo@test.com", "password", "wrongpass");

            // When / Then — mensaje genérico (A07 OWASP: no revelar si el email existe)
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Credenciales inválidas o cuenta inactiva"));
        }

        @Test
        @DisplayName("petición con content-type incorrecto retorna 415")
        void login_contentTypeIncorrecto_retorna415() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("email=test&password=123"))
                    .andExpect(status().isUnsupportedMediaType());
        }
    }

    // ── POST /api/auth/registro ───────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/auth/registro")
    class Registro {

        @Test
        @DisplayName("con datos válidos retorna 201 con token JWT")
        void registro_datosValidos_retorna201() throws Exception {
            // Given
            User nuevo = new User("María", "maria@test.com", "$2a$12$hash");
            nuevo.setRol("ROLE_USER");

            when(userService.registrar(any())).thenReturn(nuevo);
            when(jwtUtils.generateToken("maria@test.com", "ROLE_USER")).thenReturn("eyJ.nuevo.token");

            Map<String, String> body = Map.of(
                    "name", "María",
                    "email", "maria@test.com",
                    "password", "Segura123!",
                    "confirmPassword", "Segura123!"
            );

            // When / Then
            mockMvc.perform(post("/api/auth/registro")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.token").value("eyJ.nuevo.token"))
                    .andExpect(jsonPath("$.email").value("maria@test.com"));
        }

        @Test
        @DisplayName("con email duplicado retorna 409 Conflict")
        void registro_emailDuplicado_retorna409() throws Exception {
            // Given
            when(userService.registrar(any()))
                    .thenThrow(new com.aureus.exception.EmailDuplicadoException("maria@test.com"));

            Map<String, String> body = Map.of(
                    "name", "María",
                    "email", "maria@test.com",
                    "password", "Segura123!",
                    "confirmPassword", "Segura123!"
            );

            // When / Then
            mockMvc.perform(post("/api/auth/registro")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").exists());
        }
    }
}
