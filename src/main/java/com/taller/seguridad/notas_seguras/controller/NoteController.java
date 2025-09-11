package com.taller.seguridad.notas_seguras.controller;

import com.taller.seguridad.notas_seguras.model.Note;
import com.taller.seguridad.notas_seguras.model.User;
import com.taller.seguridad.notas_seguras.repository.NoteRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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

    // Crear nota
    @PostMapping
    public ResponseEntity<?> createNote(@Valid @RequestBody Note note, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).body("No autenticado");

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

        Optional<Note> noteOpt = noteRepository.findByIdAndOwner(id, user);
        if (noteOpt.isEmpty()) return ResponseEntity.status(404).body("Nota no encontrada");

        return ResponseEntity.ok(noteOpt.get());
    }

    // Actualizar una nota propia
    @PutMapping("/{id}")
    public ResponseEntity<?> updateNote(@PathVariable Long id, @Valid @RequestBody Note updatedNote, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).body("No autenticado");

        Optional<Note> noteOpt = noteRepository.findByIdAndOwner(id, user);
        if (noteOpt.isEmpty()) return ResponseEntity.status(404).body("Nota no encontrada");

        Note note = noteOpt.get();
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

        Optional<Note> noteOpt = noteRepository.findByIdAndOwner(id, user);
        if (noteOpt.isEmpty()) return ResponseEntity.status(404).body("Nota no encontrada");

        noteRepository.delete(noteOpt.get());
        return ResponseEntity.ok("Nota eliminada");
    }
}
