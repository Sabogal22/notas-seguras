package com.taller.seguridad.notas_seguras.controller;

import com.taller.seguridad.notas_seguras.model.User;
import com.taller.seguridad.notas_seguras.repository.UserRepository;
import com.taller.seguridad.notas_seguras.security.JwtService;
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

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    // --- DTO para el registro ---
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

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public boolean isAdmin() { return admin; }
        public void setAdmin(boolean admin) { this.admin = admin; }
    }

    // --- DTO para respuesta del perfil ---
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

    // --- Registro ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("El email ya está registrado");
        }

        User user = new User();
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.isAdmin() ? User.Role.ADMIN : User.Role.USER);
        user.setFailedAttempts(0);
        user.setLocked(false);
        user.setLockTime(null);

        userRepository.save(user);
        return ResponseEntity.ok("Usuario registrado correctamente");
    }

    // --- Login con bloqueo y JWT ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username,
                                   @RequestParam String password,
                                   HttpSession session) {
        Optional<User> userOpt = userRepository.findByEmail(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Usuario no encontrado");
        }

        User user = userOpt.get();

        // Si el usuario está bloqueado verificamos el tiempo
        if (user.isLocked()) {
            if (user.getLockTime() != null && user.getLockTime().plusMinutes(15).isBefore(LocalDateTime.now())) {
                user.setLocked(false);
                user.setFailedAttempts(0);
                user.setLockTime(null);
                userRepository.save(user);
            } else {
                return ResponseEntity.status(403).body("Cuenta bloqueada. Intenta de nuevo más tarde.");
            }
        }

        // Validar contraseña
        if (!passwordEncoder.matches(password, user.getPassword())) {
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

        // Login exitoso
        user.setFailedAttempts(0);
        user.setLocked(false);
        user.setLockTime(null);
        userRepository.save(user);

        session.setAttribute("user", user);

        // Generar token JWT
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        return ResponseEntity.ok(Map.of(
                "message", "Login exitoso",
                "token", token,
                "role", user.getRole()
        ));
    }

    // --- Perfil del usuario logueado ---
    @GetMapping("/me")
    public ResponseEntity<?> profile(@RequestHeader(value = "Authorization", required = false) String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Token no proporcionado");
        }

        String token = header.substring(7);
        try {
            var claims = jwtService.validateToken(token);
            String email = claims.getBody().getSubject();
            String role = (String) claims.getBody().get("role");
            return ResponseEntity.ok(Map.of("email", email, "role", role));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Token inválido o expirado");
        }
    }

}
