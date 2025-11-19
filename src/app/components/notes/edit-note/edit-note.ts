import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Auth } from '../../../services/auth';

@Component({
  selector: 'app-edit-note',
  imports: [CommonModule, FormsModule],
  templateUrl: './edit-note.html',
  styleUrl: './edit-note.css',
})
export class EditNote implements OnInit {
  @Input() note: any = null;
  @Output() noteUpdated = new EventEmitter<any>();
  @Output() cancelEdit = new EventEmitter<void>();

  editedNote: any = {
    title: '',
    content: '',
  };

  isLoading = false;
  errorMessage = '';
  successMessage = '';

  private apiUrl = 'http://localhost:8080/notes';

  constructor(private http: HttpClient, private auth: Auth) {}

  ngOnInit() {
    if (this.note) {
      this.editedNote = {
        title: this.note.title || '',
        content: this.note.content || '',
      };
    }
  }

  // Para los contadores de caracteres
  getTitleLength(): number {
    return this.editedNote.title.length;
  }

  getContentLength(): number {
    return this.editedNote.content.length;
  }

  // Método para guardar los cambios
  saveChanges() {
    // Validaciones
    if (!this.editedNote.title.trim() || !this.editedNote.content.trim()) {
      this.errorMessage = 'El título y el contenido son obligatorios';
      this.clearMessagesAfterDelay();
      return;
    }

    if (this.editedNote.title.length > 50) {
      this.errorMessage = 'El título no puede tener más de 50 caracteres';
      this.clearMessagesAfterDelay();
      return;
    }

    if (this.editedNote.content.length > 500) {
      this.errorMessage = 'El contenido no puede tener más de 500 caracteres';
      this.clearMessagesAfterDelay();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const noteData = {
      title: this.editedNote.title.trim(),
      content: this.editedNote.content.trim(),
    };

    this.http
      .put(`${this.apiUrl}/${this.note.id}`, noteData, this.auth.getAuthHeaders())
      .subscribe({
        next: (response: any) => {
          this.isLoading = false;
          this.successMessage = 'Nota actualizada correctamente';

          // Emitir el evento con la nota actualizada
          this.noteUpdated.emit(response);

          // Limpiar mensajes después de un tiempo
          setTimeout(() => {
            this.successMessage = '';
          }, 2000);
        },
        error: (error) => {
          this.isLoading = false;
          console.error('Error al actualizar la nota:', error);
          this.handleError(error);
          this.clearMessagesAfterDelay();
        },
      });
  }

  // Manejo de errores
  private handleError(error: any) {
    if (error.status === 0) {
      this.errorMessage = 'No se pudo conectar con el servidor.';
    } else if (error.status === 401) {
      this.errorMessage = 'Sesión expirada. Por favor, inicia sesión nuevamente.';
    } else if (error.status === 403) {
      this.errorMessage = 'No tienes permisos para editar esta nota.';
    } else if (error.status === 404) {
      this.errorMessage = 'La nota no fue encontrada.';
    } else if (error.status === 400) {
      this.errorMessage = 'Datos inválidos. Por favor, verifica la información.';
    } else {
      this.errorMessage = 'Error al actualizar la nota. Inténtalo de nuevo.';
    }
  }

  // Cancelar edición
  onCancel() {
    this.cancelEdit.emit();
  }

  // Limpiar mensajes después de un tiempo
  private clearMessagesAfterDelay() {
    setTimeout(() => {
      this.errorMessage = '';
      this.successMessage = '';
    }, 5000);
  }

  // Validación en tiempo real para el título
  onTitleInput() {
    if (this.editedNote.title.length > 50) {
      this.editedNote.title = this.editedNote.title.substring(0, 50);
    }
  }

  // Validación en tiempo real para el contenido
  onContentInput() {
    if (this.editedNote.content.length > 500) {
      this.editedNote.content = this.editedNote.content.substring(0, 500);
    }
  }

  // Método para verificar si el formulario es válido
  isFormValid(): boolean {
    return (
      this.editedNote.title.trim().length > 0 &&
      this.editedNote.content.trim().length > 0 &&
      this.editedNote.title.length <= 50 &&
      this.editedNote.content.length <= 500
    );
  }
}
