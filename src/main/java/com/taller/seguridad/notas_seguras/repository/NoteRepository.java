package com.taller.seguridad.notas_seguras.repository;

import com.taller.seguridad.notas_seguras.model.Note;
import com.taller.seguridad.notas_seguras.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByOwner(User owner);
    Optional<Note> findByIdAndOwner(Long id, User owner);
}
