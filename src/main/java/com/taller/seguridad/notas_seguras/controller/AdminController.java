package com.taller.seguridad.notas_seguras.controller;

import com.taller.seguridad.notas_seguras.model.User;
import com.taller.seguridad.notas_seguras.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
    public ResponseEntity<?> listUsers(HttpSession session) {
        User current = (User) session.getAttribute("user");
        if (current == null) return ResponseEntity.status(401).body("No autenticado");

        if (current.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(403).body("No autorizado");
        }

        return ResponseEntity.ok(userRepository.findAll());
    }
}
