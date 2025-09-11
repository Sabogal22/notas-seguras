package com.taller.seguridad.notas_seguras.controller;

import com.taller.seguridad.notas_seguras.model.User;
import com.taller.seguridad.notas_seguras.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Registro
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        if(userRepository.findByEmail(user.getEmail()).isPresent()){
            return ResponseEntity.badRequest().body("El email ya está registrado");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Asignar rol por defecto si no existe
        if(user.getRole() == null) {
            user.setRole(User.Role.USER);
        }

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

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401).body("Contraseña incorrecta");
        }

        // Guardamos info del usuario en la sesión
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

        // Devuelve solo campos importantes (sin password)
        return ResponseEntity.ok(new UserDTO(user.getEmail(), user.getRole()));
    }

    // DTO para no exponer password
    public static class UserDTO {
        private String email;
        private User.Role role;

        public UserDTO(String email, User.Role role) {
            this.email = email;
            this.role = role;
        }

        public String getEmail() {
            return email;
        }

        public User.Role getRole() {
            return role;
        }
    }
}
