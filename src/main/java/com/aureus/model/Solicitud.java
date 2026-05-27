package com.aureus.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes")
@Getter @Setter @NoArgsConstructor
public class Solicitud {

    public enum TipoSolicitud  { SOPORTE, ACCESO, INFORMACION }
    public enum EstadoSolicitud { PENDIENTE, APROBADA, RECHAZADA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creador_id", nullable = false)
    private User creador;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoSolicitud tipo;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    @Column(columnDefinition = "TEXT")
    private String nota;

    @Column(name = "abierta_en", nullable = false, updatable = false)
    private LocalDateTime abiertaEn;

    @Column(name = "cerrada_en")
    private LocalDateTime cerradaEn;

    @PrePersist
    public void antes() {
        this.abiertaEn = LocalDateTime.now();
        this.estado    = EstadoSolicitud.PENDIENTE;
    }
}
