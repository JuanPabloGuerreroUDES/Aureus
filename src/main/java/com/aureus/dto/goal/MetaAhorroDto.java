package com.aureus.dto.goal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class MetaAhorroDto {

    @NotBlank(message = "El nombre de la meta es obligatorio")
    @Size(max = 150)
    private String goalName;

    private double targetAmount;

    @NotBlank(message = "La fecha límite es obligatoria")
    private String deadline;
}
