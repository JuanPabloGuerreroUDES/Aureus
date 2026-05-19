package com.aureus.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad Categoría — clasifica transacciones y presupuestos.
 *
 * Diagrama de clases: Category
 *   - name: String
 *
 * Ejemplos: "Alimentación", "Transporte", "Entretenimiento", "Salud", "Salario"
 */
@Entity
@Table(name = "categorias",
       uniqueConstraints = @UniqueConstraint(columnNames = "nombre", name = "uk_categoria_nombre"))
@Getter
@Setter
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 60, message = "El nombre no puede superar 60 caracteres")
    @Column(name = "nombre", nullable = false, unique = true, length = 60)
    private String name;

    /**
     * Ícono de la categoría (emoji o clase CSS).
     * Ej: "🛒", "🚗", "fa-utensils"
     */
    @Column(length = 10)
    private String icono;

    // ── Constructor de conveniencia ───────────────────────────────────────

    public Category(String name) {
        this.name = name;
    }

    public Category(String name, String icono) {
        this.name = name;
        this.icono = icono;
    }

    @Override
    public String toString() {
        return "Category{id=" + id + ", name='" + name + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category c)) return false;
        return id != null && id.equals(c.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
