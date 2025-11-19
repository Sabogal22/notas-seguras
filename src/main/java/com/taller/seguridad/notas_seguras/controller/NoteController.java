package com.taller.seguridad.notas_seguras.controller;

import com.taller.seguridad.notas_seguras.model.Note;
import com.taller.seguridad.notas_seguras.model.User;
import com.taller.seguridad.notas_seguras.repository.NoteRepository;
import com.taller.seguridad.notas_seguras.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/notes")
@CrossOrigin(origins = "http://localhost:4200")
public class NoteController {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    // DTO para validación de entrada
    public static class NoteDTO {
        @NotBlank(message = "El título es obligatorio")
        private String title;

        @NotBlank(message = "El contenido es obligatorio")
        private String content;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    // Método para obtener el usuario actual desde Spring Security
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String email = authentication.getName();
        System.out.println("Getting current user with email: " + email);

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("User found: " + user.getEmail() + ", ID: " + user.getId());
            return user;
        } else {
            System.out.println("User not found for email: " + email);
            return null;
        }
    }

    // Crear nota
    @PostMapping
    public ResponseEntity<?> createNote(@Valid @RequestBody NoteDTO noteDTO) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        System.out.println("Creating note for user: " + user.getEmail());

        Note note = new Note();
        note.setTitle(noteDTO.getTitle());
        note.setContent(noteDTO.getContent());
        note.setOwner(user);

        Note savedNote = noteRepository.save(note);
        return ResponseEntity.ok(savedNote);
    }

    // Listar mis notas
    @GetMapping
    public ResponseEntity<?> getMyNotes() {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        List<Note> notes = noteRepository.findByOwner(user);
        return ResponseEntity.ok(notes);
    }

    // Obtener una nota propia por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getNote(@PathVariable Long id) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        Optional<Note> noteOpt = noteRepository.findById(id);
        if (noteOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Nota no encontrada");
        }

        Note note = noteOpt.get();
        if (!note.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("No tienes permiso para ver esta nota");
        }

        return ResponseEntity.ok(note);
    }

    // Actualizar una nota propia
    @PutMapping("/{id}")
    public ResponseEntity<?> updateNote(@PathVariable Long id,
                                        @Valid @RequestBody NoteDTO updatedNote) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        Optional<Note> noteOpt = noteRepository.findById(id);
        if (noteOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Nota no encontrada");
        }

        Note note = noteOpt.get();
        if (!note.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("No tienes permiso para editar esta nota");
        }

        note.setTitle(updatedNote.getTitle());
        note.setContent(updatedNote.getContent());
        noteRepository.save(note);

        return ResponseEntity.ok(note);
    }

    // Eliminar una nota propia
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNote(@PathVariable Long id) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        Optional<Note> noteOpt = noteRepository.findById(id);
        if (noteOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Nota no encontrada");
        }

        Note note = noteOpt.get();
        if (!note.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("No tienes permiso para eliminar esta nota");
        }

        noteRepository.delete(note);
        return ResponseEntity.ok("Nota eliminada");
    }

    // Endpoint de diagnóstico
    @GetMapping("/debug-auth")
    public ResponseEntity<?> debugAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return ResponseEntity.ok("No authentication found");
        }

        Map<String, Object> authInfo = new java.util.HashMap<>();
        authInfo.put("authenticated", authentication.isAuthenticated());
        authInfo.put("name", authentication.getName());
        authInfo.put("authorities", authentication.getAuthorities());
        authInfo.put("principal", authentication.getPrincipal().getClass().getSimpleName());

        // Obtener usuario de la base de datos
        String email = authentication.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        authInfo.put("user_in_database", userOpt.isPresent());
        if (userOpt.isPresent()) {
            authInfo.put("user_email", userOpt.get().getEmail());
            authInfo.put("user_id", userOpt.get().getId());
        }

        return ResponseEntity.ok(authInfo);
    }
}