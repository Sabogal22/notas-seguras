import { CommonModule } from '@angular/common';
import { Component, Output, EventEmitter, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Auth } from '../../../services/auth';

@Component({
  selector: 'app-list-notes',
  imports: [CommonModule, FormsModule],
  templateUrl: './list-notes.html',
  styleUrl: './list-notes.css',
})
export class ListNotes implements OnInit {
  @Output() notesCountChange = new EventEmitter<number>();

  selectedNote: any = null;
  showModal = false;
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  notes: any[] = [];
  private apiUrl = 'http://localhost:8080/notes';

  constructor(
    private http: HttpClient,
    private router: Router,
    private auth: Auth // Inyecta el servicio Auth
  ) {}

  ngOnInit() {
    this.loadNotes();
  }

  // Cargar notas desde la API
  loadNotes() {
    if (!this.auth.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.http.get(this.apiUrl, this.auth.getAuthHeaders()).subscribe({
      next: (response: any) => {
        this.isLoading = false;
        this.notes = response;
        this.emitNotesCount();
        console.log('Notas cargadas:', this.notes);
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Error al cargar notas:', error);
        
        if (error.status === 401) {
          this.errorMessage = 'No autenticado. Redirigiendo al login...';
          this.auth.removeToken();
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 2000);
        } else {
          this.errorMessage = 'Error al cargar las notas. Inténtalo de nuevo.';
          this.clearMessagesAfterDelay();
        }
      }
    });
  }

  openEditModal(note: any) {
    this.selectedNote = { ...note };
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
    this.selectedNote = null;
  }

  // Guardar cambios en la nota
  saveChanges() {
    if (!this.selectedNote) return;

    this.isLoading = true;
    this.errorMessage = '';

    this.http.put(`${this.apiUrl}/${this.selectedNote.id}`, 
      {
        title: this.selectedNote.title,
        content: this.selectedNote.content
      }, 
      this.auth.getAuthHeaders()
    ).subscribe({
      next: (response: any) => {
        this.isLoading = false;
        this.successMessage = 'Nota actualizada correctamente';
        
        // Actualizar la lista local
        const index = this.notes.findIndex(note => note.id === this.selectedNote.id);
        if (index !== -1) {
          this.notes[index] = response;
        }
        
        this.closeModal();
        this.clearMessagesAfterDelay();
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Error al actualizar nota:', error);
        this.errorMessage = 'Error al actualizar la nota. Inténtalo de nuevo.';
        this.clearMessagesAfterDelay();
      }
    });
  }

  // Eliminar nota
  deleteNote(note: any) {
    if (!confirm('¿Estás seguro de que quieres eliminar esta nota?')) {
      return;
    }

    this.isLoading = true;
    this.http.delete(`${this.apiUrl}/${note.id}`, this.auth.getAuthHeaders()).subscribe({
      next: () => {
        this.isLoading = false;
        this.successMessage = 'Nota eliminada correctamente';
        
        // Eliminar de la lista local
        this.notes = this.notes.filter(n => n.id !== note.id);
        this.emitNotesCount();
        this.clearMessagesAfterDelay();
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Error al eliminar nota:', error);
        this.errorMessage = 'Error al eliminar la nota. Inténtalo de nuevo.';
        this.clearMessagesAfterDelay();
      }
    });
  }

  // Nueva función para emitir el conteo
  private emitNotesCount() {
    this.notesCountChange.emit(this.notes.length);
  }

  // Limpiar mensajes después de un tiempo
  private clearMessagesAfterDelay() {
    setTimeout(() => {
      this.errorMessage = '';
      this.successMessage = '';
    }, 5000);
  }
}