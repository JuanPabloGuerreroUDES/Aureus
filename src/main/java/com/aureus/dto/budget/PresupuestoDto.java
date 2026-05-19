package com.aureus.dto.budget;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class PresupuestoDto {
    private double limitAmount;
    private String startDate;
    private String endDate;
    private Long categoryId;
    private Long accountId;
    private int umbralAlerta = 80;
}
