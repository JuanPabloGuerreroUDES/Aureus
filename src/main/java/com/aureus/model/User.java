package com.aureus.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Usuario — raíz del modelo de dominio.
 *
 * Corresponde al diagrama de clases: User
 *   - name: String
 *   - email: String
 *   - password: String
 *   + authenticate(): boolean
 *   + getBalance(): double
 *
 * Notas de seguridad (Unidad 9):
 *   - La contraseña se almacena como hash BCrypt (NUNCA en texto claro).
 *   - El campo 'rol' sigue la convención de Spring Security: "ROLE_USER" / "ROLE_ADMIN".
 *   - 'activo' permite deshabilitar cuentas sin eliminarlas (soft delete).
 */
@Entity
@Table(name = "usuarios",
       uniqueConstraints = @UniqueConstraint(columnNames = "email", name = "uk_usuario_email"))
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Atributos del diagrama ────────────────────────────────────────────

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Hash BCrypt de la contraseña.
     * Nunca exponer este campo en DTOs ni vistas.
     * Spring Security verifica con passwordEncoder.matches() automáticamente.
     */
    @NotBlank(message = "La contraseña es obligatoria")
    @Column(nullable = false)
    private String password;

    // ── Campos de seguridad y autorización ───────────────────────────────

    /**
     * Rol del usuario. Convención Spring Security: "ROLE_USER" o "ROLE_ADMIN".
     * Para múltiples roles se usaría una tabla separada; aquí un solo rol por simplicidad.
     */
    @Column(nullable = false, length = 20)
    private String rol = "ROLE_USER";

    /**
     * Cuenta activa. Permite deshabilitar sin borrar (GDPR-friendly).
     * UserDetailsService usa este campo en .disabled(!user.isActivo()).
     */
    @Column(nullable = false)
    private boolean activo = true;

    // ── Relaciones ───────────────────────────────────────────────────────

    /**
     * Un usuario puede tener varias cuentas financieras.
     * CascadeType.ALL: al eliminar el usuario se eliminan sus cuentas.
     * orphanRemoval = true: si se quita una cuenta de la lista, se borra de BD.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Account> accounts = new ArrayList<>();

    /**
     * Un usuario puede tener varias metas de ahorro.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SavingsGoal> savingsGoals = new ArrayList<>();

    // ── Constructor de conveniencia ───────────────────────────────────────

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // ── Métodos del diagrama ──────────────────────────────────────────────

    /**
     * Verifica si el usuario está habilitado para autenticarse.
     * La lógica real de autenticación la maneja Spring Security;
     * este método es un helper del dominio.
     *
     * @return true si la cuenta está activa.
     */
    public boolean authenticate() {
        return this.activo;
    }

    /**
     * Calcula el balance total del usuario sumando los balances
     * de todas sus cuentas.
     *
     * @return suma de balances de todas las cuentas del usuario.
     */
    public double getBalance() {
        return accounts.stream()
                .mapToDouble(Account::getBalance)
                .sum();
    }

    // ── Helpers de relaciones ─────────────────────────────────────────────

    public void addAccount(Account account) {
        accounts.add(account);
        account.setUser(this);
    }

    public void removeAccount(Account account) {
        accounts.remove(account);
        account.setUser(null);
    }

    public void addSavingsGoal(SavingsGoal goal) {
        savingsGoals.add(goal);
        goal.setUser(this);
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "', rol='" + rol + "'}";
    }
}
