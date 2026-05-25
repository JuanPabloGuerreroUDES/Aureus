package com.aureus.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class CuentaDto {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String name;

    /** Emoji opcional (🏦, 🐷, 💳, 💰, 📦...) */
    @Size(max = 10)
    private String icono;

    @Size(max = 255)
    private String descripcion;
}
