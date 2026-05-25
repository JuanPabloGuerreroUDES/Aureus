package com.aureus.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO del formulario de registro de usuario.
 * Documentado con @Schema para Swagger UI (U11 §7.4).
 */
@Getter @Setter @NoArgsConstructor
@Schema(description = "Datos requeridos para registrar un nuevo usuario")
public class RegistroDto {

    @Schema(description = "Nombre completo del usuario", example = "Juan García")
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String name;

    @Schema(description = "Correo electrónico — será el username para login",
            example = "juan.garcia@email.com")
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    private String email;

    @Schema(description = "Contraseña (mínimo 8 caracteres)", example = "Segura123!")
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "Mínimo 8 caracteres")
    private String password;

    @Schema(description = "Confirmación de contraseña (debe coincidir con password)",
            example = "Segura123!")
    @NotBlank(message = "La confirmación es obligatoria")
    private String confirmPassword;

    public boolean passwordsCoinciden() {
        return password != null && password.equals(confirmPassword);
    }
}
