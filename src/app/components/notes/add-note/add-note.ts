import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Auth } from '../../../services/auth';

@Component({
  selector: 'app-add-note',
  imports: [CommonModule, FormsModule],
  templateUrl: './add-note.html',
  styleUrl: './add-note.css',
})
export class AddNote {
  note = {
    title: '',
    content: '',
  };

  isLoading = false;
  errorMessage = '';
  successMessage = '';

  private apiUrl = 'http://localhost:8080/notes';

  constructor(
    private http: HttpClient,
    private router: Router,
    private auth: Auth // Inyecta el servicio Auth
  ) {}

  // Para los contadores de caracteres
  getTitleLength(): number {
    return this.note.title.length;
  }

  getContentLength(): number {
    return this.note.content.length;
  }

  // Método para guardar la nota
  onSubmit() {
    // Validaciones
    if (!this.note.title.trim() || !this.note.content.trim()) {
      this.errorMessage = 'El título y el contenido son obligatorios';
      this.clearMessagesAfterDelay();
      return;
    }

    if (!this.auth.isAuthenticated()) {
      this.errorMessage = 'Debes iniciar sesión para crear notas';
      this.clearMessagesAfterDelay();
      this.router.navigate(['/login']);
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const noteData = {
      title: this.note.title.trim(),
      content: this.note.content.trim(),
    };

    // Usar los headers del servicio Auth
    this.http.post(this.apiUrl, noteData, this.auth.getAuthHeaders()).subscribe({
      next: (response: any) => {
        this.isLoading = false;
        this.successMessage = '¡Nota guardada exitosamente!';

        // Limpiar el formulario
        this.onClear();

        // Redirigir a la lista de notas
        setTimeout(() => {
          this.router.navigate(['/notes']);
        }, 1500);
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Error al guardar la nota:', error);

        this.handleError(error);
        this.clearMessagesAfterDelay();
      },
    });
  }

  // Manejo específico de errores
  private handleError(error: any) {
    if (error.status === 0) {
      this.errorMessage =
        'No se pudo conectar con el servidor. Verifica que Spring Boot esté ejecutándose.';
    } else if (error.status === 401) {
      this.errorMessage = 'Sesión expirada. Por favor, inicia sesión nuevamente.';
      setTimeout(() => {
        this.auth.removeToken();
        this.router.navigate(['/login']);
      }, 2000);
    } else if (error.status === 403) {
      this.errorMessage = 'No tienes permisos para realizar esta acción.';
    } else if (error.status === 400) {
      this.errorMessage = 'Datos inválidos: título y contenido son obligatorios.';
    } else {
      this.errorMessage = 'Error al guardar la nota. Inténtalo de nuevo.';
    }
  }

  onClear() {
    this.note = { title: '', content: '' };
    this.errorMessage = '';
    this.successMessage = '';
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
    if (this.note.title.length > 50) {
      this.note.title = this.note.title.substring(0, 50);
    }
  }

  // Validación en tiempo real para el contenido
  onContentInput() {
    if (this.note.content.length > 500) {
      this.note.content = this.note.content.substring(0, 500);
    }
  }

  // Método para verificar si el formulario es válido
  isFormValid(): boolean {
    return (
      this.note.title.trim().length > 0 &&
      this.note.content.trim().length > 0 &&
      this.note.title.length <= 50 &&
      this.note.content.length <= 500
    );
  }
}
