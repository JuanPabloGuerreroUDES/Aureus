package com.aureus.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class ActualizarPerfilDto {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String name;
}
