package com.taller.seguridad.notas_seguras.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El email no es válido")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 10, message = "La contraseña debe tener al menos 10 caracteres")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "La contraseña debe tener al menos una mayúscula, una minúscula y un número"
    )
    private String password;

    @Enumerated(EnumType.STRING) // Se guarda como texto en la BD
    @Column(nullable = false)
    private Role role;

    // Control de intentos fallidos y bloqueo
    private int failedAttempts = 0;

    private boolean locked = false; // bloqueo manual o permanente

    @Column(name = "lock_time")
    private LocalDateTime lockTime; // bloqueo temporal

    @Column(name = "last_login")
    private LocalDateTime lastLogin; // auditoría (opcional)

    // Enum de roles
    public enum Role {
        USER, ADMIN
    }

    // --- Métodos de ayuda para login ---

    public void incrementFailedAttempts() {
        this.failedAttempts++;
    }

    public void resetFailedAttempts() {
        this.failedAttempts = 0;
    }

    public void lock() {
        this.locked = true;
        this.lockTime = LocalDateTime.now();
    }

    /**
     * Verifica si el tiempo de bloqueo ya expiró
     */
    public boolean isLockTimeExpired() {
        if (this.lockTime == null) return false;
        return this.lockTime.plusMinutes(LOCK_TIME_DURATION).isBefore(LocalDateTime.now());
    }

    /**
     * Desbloquea al usuario si ya expiró el tiempo de bloqueo
     */
    public void unlockIfLockTimeExpired() {
        if (isLocked() && isLockTimeExpired()) {
            this.locked = false;
            this.lockTime = null;
            this.failedAttempts = 0;
        }
    }

    // --- Getters y Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public LocalDateTime getLockTime() { return lockTime; }
    public void setLockTime(LocalDateTime lockTime) { this.lockTime = lockTime; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    // --- Constantes de seguridad ---
    public static final int MAX_FAILED_ATTEMPTS = 3; // intentos máximos
    public static final int LOCK_TIME_DURATION = 15; // minutos de bloqueo temporal
}
