package com.taller.seguridad.notas_seguras.controller;

import com.taller.seguridad.notas_seguras.model.User;
import com.taller.seguridad.notas_seguras.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Control de intentos fallidos
    private final Map<String, Integer> attempts = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lockoutUntil = new ConcurrentHashMap<>();

    // DTO para el registro (validación fuerte)
    public static class RegisterRequest {
        @Email
        private String email;

        @NotBlank
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{10,}$",
                message = "Password debe tener ≥10 caracteres, 1 mayúscula, 1 minúscula y 1 dígito"
        )
        private String password;

        private boolean admin;

        // getters y setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public boolean isAdmin() { return admin; }
        public void setAdmin(boolean admin) { this.admin = admin; }
    }

    // DTO para respuesta de usuario
    public static class UserDTO {
        private String email;
        private User.Role role;

        public UserDTO(String email, User.Role role) {
            this.email = email;
            this.role = role;
        }

        public String getEmail() { return email; }
        public User.Role getRole() { return role; }
    }

    // Registro
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("El email ya está registrado");
        }

        User user = new User();
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.isAdmin() ? User.Role.ADMIN : User.Role.USER);

        userRepository.save(user);
        return ResponseEntity.ok("Usuario registrado correctamente");
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username,
                                        @RequestParam String password,
                                        HttpSession session) {
        Optional<User> userOpt = userRepository.findByEmail(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Usuario no encontrado");
        }

        User user = userOpt.get();

        // Si está bloqueado, verificamos si pasaron 15 minutos
        if (user.isLocked()) {
            if (user.getLockTime() != null && user.getLockTime().plusMinutes(15).isBefore(LocalDateTime.now())) {
                // desbloqueamos
                user.setLocked(false);
                user.setFailedAttempts(0);
                user.setLockTime(null);
                userRepository.save(user);
            } else {
                return ResponseEntity.status(403).body("Cuenta bloqueada. Intenta de nuevo más tarde.");
            }
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            // intento fallido
            user.setFailedAttempts(user.getFailedAttempts() + 1);

            if (user.getFailedAttempts() >= 5) {
                user.setLocked(true);
                user.setLockTime(LocalDateTime.now());
                userRepository.save(user);
                return ResponseEntity.status(403).body("Cuenta bloqueada por demasiados intentos fallidos. Intenta en 15 minutos.");
            }

            userRepository.save(user);
            return ResponseEntity.status(401).body("Contraseña incorrecta. Intentos fallidos: " + user.getFailedAttempts());
        }

        // login exitoso → reiniciamos intentos
        user.setFailedAttempts(0);
        user.setLockTime(null);
        user.setLocked(false);
        userRepository.save(user);

        session.setAttribute("user", user);

        return ResponseEntity.ok("Login exitoso");
    }

    // Perfil del usuario logueado (/auth/me)
    @GetMapping("/me")
    public ResponseEntity<?> profile(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }
        return ResponseEntity.ok(new UserDTO(user.getEmail(), user.getRole()));
    }

    // --- Métodos de lockout ---
    private boolean isLocked(String email) {
        LocalDateTime until = lockoutUntil.get(email);
        return until != null && until.isAfter(LocalDateTime.now());
    }

    private void loginFailed(String email) {
        int newAttempts = attempts.getOrDefault(email, 0) + 1;
        attempts.put(email, newAttempts);
        if (newAttempts >= 5) {
            lockoutUntil.put(email, LocalDateTime.now().plusMinutes(15));
        }
    }

    private void loginSucceeded(String email) {
        attempts.remove(email);
        lockoutUntil.remove(email);
    }
}
