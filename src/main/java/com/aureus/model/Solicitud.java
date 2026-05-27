package com.aureus.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad para el sistema de solicitudes con flujo de estados (Módulo 2 — Examen Final).
 *
 * Ciclo de vida: PENDIENTE → APROBADA | RECHAZADA
 */
@Entity
@Table(name = "solicitudes")
@Getter
@Setter
@NoArgsConstructor
public class Solicitud {

    public enum TipoSolicitud { SOPORTE, ACCESO, INFORMACION }

    public enum EstadoSolicitud { PENDIENTE, APROBADA, RECHAZADA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Usuario que radicó la solicitud. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private User solicitante;

    @NotNull(message = "El tipo de solicitud es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoSolicitud tipo;

    @NotBlank(message = "La descripción es obligatoria")
    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    /** Estado actual — inicia siempre en PENDIENTE. */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    /** Observación del administrador al aprobar o rechazar. */
    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observacion;

    /** Fecha de creación — generada automáticamente. */
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /** Fecha de resolución — se registra automáticamente al aprobar o rechazar. */
    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @PrePersist
    private void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = EstadoSolicitud.PENDIENTE;
    }
}
