package com.aureus.service;

import com.aureus.dto.auth.RegistroDto;
import com.aureus.exception.EmailDuplicadoException;
import com.aureus.exception.ValidacionException;
import com.aureus.model.User;
import com.aureus.repository.UserRepository;
import com.aureus.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de UserServiceImpl (U10 §2 — JUnit 5 + Mockito).
 *
 * Patrón de nomenclatura (U10 §1.5): metodo_condicion_resultadoEsperado
 * Estructura Given-When-Then en cada prueba.
 * Clases @Nested para agrupar por escenario (U10 §2.5).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl")
class UserServiceImplTest {

    @Mock
    private UserRepository  userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    // ── Tests de registro ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Al registrar un usuario")
    class Registro {

        private RegistroDto dto;

        @BeforeEach
        void setUp() {
            dto = new RegistroDto();
            dto.setName("Juan García");
            dto.setEmail("juan@test.com");
            dto.setPassword("Segura123!");
            dto.setConfirmPassword("Segura123!");
        }

        @Test
        @DisplayName("con datos válidos guarda el usuario con contraseña hasheada")
        void registrar_conDatosValidos_guardaUsuarioConHashBCrypt() {
            // Given
            when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(dto.getPassword())).thenReturn("$2a$12$hash");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            User resultado = userService.registrar(dto);

            // Then
            assertThat(resultado.getEmail()).isEqualTo("juan@test.com");
            assertThat(resultado.getPassword()).isEqualTo("$2a$12$hash");
            assertThat(resultado.getRol()).isEqualTo("ROLE_USER");
            assertThat(resultado.isActivo()).isTrue();
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("con email duplicado lanza EmailDuplicadoException")
        void registrar_conEmailDuplicado_lanzaEmailDuplicadoException() {
            // Given
            when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

            // When / Then
            assertThatThrownBy(() -> userService.registrar(dto))
                    .isInstanceOf(EmailDuplicadoException.class)
                    .hasMessageContaining("juan@test.com");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("con contraseñas diferentes lanza ValidacionException")
        void registrar_conPasswordsDiferentes_lanzaValidacionException() {
            // Given
            dto.setConfirmPassword("diferente");

            // When / Then
            assertThatThrownBy(() -> userService.registrar(dto))
                    .isInstanceOf(ValidacionException.class)
                    .hasMessageContaining("no coinciden");

            verify(userRepository, never()).save(any());
        }
    }

    // ── Tests de búsqueda ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Al buscar un usuario")
    class Busqueda {

        @Test
        @DisplayName("por email existente retorna el usuario")
        void buscarPorEmail_cuandoExiste_retornaUsuario() {
            // Given
            User user = new User("Test", "test@test.com", "$2a$12$hash");
            when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

            // When
            User resultado = userService.buscarPorEmail("test@test.com");

            // Then
            assertThat(resultado.getEmail()).isEqualTo("test@test.com");
        }

        @Test
        @DisplayName("por email no existente lanza RecursoNoEncontradoException")
        void buscarPorEmail_cuandoNoExiste_lanzaExcepcion() {
            // Given
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> userService.buscarPorEmail("noexiste@test.com"))
                    .isInstanceOf(com.aureus.exception.RecursoNoEncontradoException.class);
        }
    }
}
