package com.taller.seguridad.notas_seguras.controller;

import com.taller.seguridad.notas_seguras.model.Note;
import com.taller.seguridad.notas_seguras.model.User;
import com.taller.seguridad.notas_seguras.repository.NoteRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/notes")
public class NoteController {

    @Autowired
    private NoteRepository noteRepository;

    // DTO para validación de entrada
    public static class NoteDTO {
        @NotBlank
        private String title;
        @NotBlank
        private String content;

        // getters y setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    // Crear nota
    @PostMapping
    public ResponseEntity<?> createNote(@Valid @RequestBody NoteDTO noteDTO, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).body("No autenticado");

        Note note = new Note();
        note.setTitle(noteDTO.getTitle());
        note.setContent(noteDTO.getContent());
        note.setOwner(user);

        noteRepository.save(note);
        return ResponseEntity.ok(note);
    }

    // Listar mis notas
    @GetMapping
    public ResponseEntity<?> getMyNotes(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).body("No autenticado");

        List<Note> notes = noteRepository.findByOwner(user);
        return ResponseEntity.ok(notes);
    }

    // Obtener una nota propia por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getNote(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).body("No autenticado");

        Optional<Note> noteOpt = noteRepository.findById(id);
        if (noteOpt.isEmpty()) return ResponseEntity.status(404).body("Nota no encontrada");

        Note note = noteOpt.get();
        if (!note.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("No tienes permiso para ver esta nota");
        }

        return ResponseEntity.ok(note);
    }

    // Actualizar una nota propia
    @PutMapping("/{id}")
    public ResponseEntity<?> updateNote(@PathVariable Long id, @Valid @RequestBody NoteDTO updatedNote, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).body("No autenticado");

        Optional<Note> noteOpt = noteRepository.findById(id);
        if (noteOpt.isEmpty()) return ResponseEntity.status(404).body("Nota no encontrada");

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
    public ResponseEntity<?> deleteNote(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).body("No autenticado");

        Optional<Note> noteOpt = noteRepository.findById(id);
        if (noteOpt.isEmpty()) return ResponseEntity.status(404).body("Nota no encontrada");

        Note note = noteOpt.get();
        if (!note.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("No tienes permiso para eliminar esta nota");
        }

        noteRepository.delete(note);
        return ResponseEntity.ok("Nota eliminada");
    }
}
