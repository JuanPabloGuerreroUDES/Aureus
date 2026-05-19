package com.aureus.service;

import com.aureus.dto.goal.AporteMetaDto;
import com.aureus.dto.goal.MetaAhorroDto;
import com.aureus.exception.RecursoNoEncontradoException;
import com.aureus.model.SavingsGoal;
import com.aureus.model.User;
import com.aureus.repository.SavingsGoalRepository;
import com.aureus.service.impl.SavingsGoalServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SavingsGoalServiceImpl")
class SavingsGoalServiceImplTest {

    @Mock
    private SavingsGoalRepository goalRepository;

    @InjectMocks
    private SavingsGoalServiceImpl goalService;

    private User usuario;

    @BeforeEach
    void setUp() {
        usuario = new User("Juan", "juan@test.com", "hash");
    }

    @Nested
    @DisplayName("Al crear una meta")
    class Creacion {

        @Test
        @DisplayName("con datos válidos persiste la meta")
        void crear_conDatosValidos_persisteMeta() {
            // Given
            MetaAhorroDto dto = new MetaAhorroDto();
            dto.setGoalName("Fondo emergencia");
            dto.setTargetAmount(5000.0);
            dto.setDeadline(LocalDate.now().plusMonths(6).toString());

            when(goalRepository.save(any(SavingsGoal.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // When
            SavingsGoal meta = goalService.crear(dto, usuario);

            // Then
            assertThat(meta.getGoalName()).isEqualTo("Fondo emergencia");
            assertThat(meta.getTargetAmount().doubleValue()).isEqualTo(5000.0);
            verify(goalRepository, times(1)).save(any(SavingsGoal.class));
        }
    }

    @Nested
    @DisplayName("Al calcular el progreso")
    class Progreso {

        @ParameterizedTest(name = "aportado={0}, objetivo={1} → progreso={2}%")
        @CsvSource({
            "2500.0, 5000.0, 50.0",
            "5000.0, 5000.0, 100.0",
            "0.0,    5000.0, 0.0",
            "6000.0, 5000.0, 100.0"  // no supera 100%
        })
        @DisplayName("calcula correctamente el porcentaje")
        void calculateProgress_variosEscenarios_retornaProgresoCorrect(
                double aportado, double objetivo, double esperado) {

            SavingsGoal meta = new SavingsGoal("Meta", objetivo,
                    LocalDate.now().plusMonths(3), usuario);
            if (aportado > 0) meta.registrarAporte(Math.min(aportado, objetivo));

            assertThat(meta.calculateProgress()).isEqualTo(esperado);
        }
    }

    @Nested
    @DisplayName("Al registrar un aporte")
    class Aportes {

        @Test
        @DisplayName("con meta existente suma el monto al current")
        void registrarAporte_cuandoMetaExiste_sumaAlMonto() {
            // Given
            SavingsGoal meta = new SavingsGoal("Meta", 3000,
                    LocalDate.now().plusMonths(6), usuario);
            meta.registrarAporte(500);

            AporteMetaDto dto = new AporteMetaDto();
            dto.setGoalId(1L);
            dto.setAporte(200.0);

            when(goalRepository.findByIdAndUser(1L, usuario)).thenReturn(Optional.of(meta));
            when(goalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // When
            SavingsGoal resultado = goalService.registrarAporte(dto, usuario);

            // Then
            assertThat(resultado.getCurrentAmount().doubleValue()).isEqualTo(700.0);
        }

        @Test
        @DisplayName("con meta no encontrada lanza RecursoNoEncontradoException")
        void registrarAporte_cuandoMetaNoExiste_lanzaExcepcion() {
            // Given
            AporteMetaDto dto = new AporteMetaDto();
            dto.setGoalId(99L);
            dto.setAporte(100.0);

            when(goalRepository.findByIdAndUser(99L, usuario)).thenReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> goalService.registrarAporte(dto, usuario))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }
    }
}
