package com.aureus.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** DTO del formulario de registro de usuario. */
@Getter @Setter @NoArgsConstructor
public class RegistroDto {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "Mínimo 8 caracteres")
    private String password;

    @NotBlank(message = "La confirmación es obligatoria")
    private String confirmPassword;

    public boolean passwordsCoinciden() {
        return password != null && password.equals(confirmPassword);
    }
}
