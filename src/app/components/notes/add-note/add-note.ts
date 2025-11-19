import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-add-note',
  imports: [CommonModule, FormsModule],
  templateUrl: './add-note.html',
  styleUrl: './add-note.css',
})
export class AddNote {
  note = {
    title: '',
    content: ''
  };

  isLoading = false;
  errorMessage = '';
  successMessage = '';

  // Configuración de la API - ajusta según tu entorno
  private apiUrl = 'http://localhost:8080/notes'; // Cambia por tu URL de Spring Boot

  constructor(
    private http: HttpClient,
    private router: Router
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

    if (this.note.title.length > 50) {
      this.errorMessage = 'El título no puede tener más de 50 caracteres';
      this.clearMessagesAfterDelay();
      return;
    }

    if (this.note.content.length > 500) {
      this.errorMessage = 'El contenido no puede tener más de 500 caracteres';
      this.clearMessagesAfterDelay();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    // Preparar los datos para enviar (sin createdAt ya que tu backend lo maneja)
    const noteData = {
      title: this.note.title.trim(),
      content: this.note.content.trim()
    };

    // Configurar headers - importante incluir credentials para las sesiones
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      }),
      withCredentials: true // Esto es crucial para enviar cookies de sesión
    };

    // Enviar a la API de Spring Boot
    this.http.post(this.apiUrl, noteData, httpOptions).subscribe({
      next: (response: any) => {
        this.isLoading = false;
        this.successMessage = '¡Nota guardada exitosamente!';
        
        // Limpiar el formulario
        this.onClear();
        
        // Opcional: redirigir a la lista de notas
        setTimeout(() => {
          this.router.navigate(['/notes']);
        }, 1500);
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Error al guardar la nota:', error);
        
        this.handleError(error);
        this.clearMessagesAfterDelay();
      }
    });
  }

  // Manejo específico de errores según tu API
  private handleError(error: any) {
    if (error.status === 0) {
      this.errorMessage = 'No se pudo conectar con el servidor. Verifica que Spring Boot esté ejecutándose.';
    } else if (error.status === 401) {
      this.errorMessage = 'No autenticado. Por favor, inicia sesión.';
      // Redirigir al login después de un tiempo
      setTimeout(() => {
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
    return this.note.title.trim().length > 0 && 
           this.note.content.trim().length > 0 &&
           this.note.title.length <= 50 &&
           this.note.content.length <= 500;
  }
}