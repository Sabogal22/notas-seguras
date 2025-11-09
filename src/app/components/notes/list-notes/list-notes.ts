import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-list-notes',
  imports: [CommonModule, FormsModule],
  templateUrl: './list-notes.html',
  styleUrl: './list-notes.css',
})
export class ListNotes {
  selectedNote: any = null;
  showModal = false;

  notes = [
    { 
      id: 1,
      title: 'Primera Nota', 
      content: 'Esta es una nota de ejemplo con contenido corto.' 
    },
    { 
      id: 2,
      title: 'Segunda Nota', 
      content: 'Otra nota para ver como se ve el diseño del listado.' 
    }
  ]

  openEditModal(note: any) {
    this.selectedNote = { ...note };
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
    this.selectedNote = null;
  }

  saveChanges() {
    if (!this.selectedNote) return;
    
    // Encontrar la nota original y actualizarla
    const index = this.notes.findIndex(note => note.id === this.selectedNote.id);
    if (index !== -1) {
      this.notes[index] = { ...this.selectedNote };
    }
    
    this.closeModal();
  }

  deleteNote(note: any) {
    // Filtrar el array para eliminar la nota
    this.notes = this.notes.filter(n => n !== note);
  }
}