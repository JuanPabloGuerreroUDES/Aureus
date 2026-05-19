package com.aureus.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class CambiarPasswordDto {

    @NotBlank
    private String passwordActual;

    @NotBlank
    @Size(min = 8, max = 100)
    private String passwordNuevo;

    @NotBlank
    private String confirmarPassword;

    public boolean passwordsCoinciden() {
        return passwordNuevo != null && passwordNuevo.equals(confirmarPassword);
    }
}
