package com.aureus.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad para el sistema de mensajería interna (Módulo 1 — Examen Final).
 * Los mensajes se almacenan en BD y solo son accesibles via los endpoints descritos.
 */
@Entity
@Table(name = "mensajes")
@Getter
@Setter
@NoArgsConstructor
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Usuario que envió el mensaje. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emisor_id", nullable = false)
    private User emisor;

    /** Usuario al que va dirigido el mensaje. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receptor_id", nullable = false)
    private User receptor;

    @NotBlank(message = "El asunto es obligatorio")
    @Size(max = 200, message = "El asunto no puede superar 200 caracteres")
    @Column(name = "asunto", nullable = false, length = 200)
    private String asunto;

    @NotBlank(message = "El contenido es obligatorio")
    @Column(name = "contenido", nullable = false, columnDefinition = "TEXT")
    private String contenido;

    /** Si el receptor ya leyó el mensaje. */
    @Column(name = "leido", nullable = false)
    private boolean leido = false;

    /** Fecha y hora de envío — se genera automáticamente al crear. */
    @Column(name = "enviado_en", nullable = false, updatable = false)
    private LocalDateTime enviadoEn;

    @PrePersist
    private void prePersist() {
        this.enviadoEn = LocalDateTime.now();
    }
}
