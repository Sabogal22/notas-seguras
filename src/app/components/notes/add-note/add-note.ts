import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms'; // ← Agregar esto
import { CommonModule } from '@angular/common'; // ← También esto si usas directivas

@Component({
  selector: 'app-add-note',
  imports: [CommonModule, FormsModule], // ← Agregar ambos
  templateUrl: './add-note.html',
  styleUrl: './add-note.css',
})
export class AddNote {
  note = {
    title: '',
    content: ''
  };

  // Para los contadores de caracteres
  getTitleLength(): number {
    return this.note.title.length;
  }

  getContentLength(): number {
    return this.note.content.length;
  }

  // Métodos para futura implementación
  onSubmit() {
    console.log('Nota guardada:', this.note);
    // Aquí irá la lógica para guardar
  }

  onClear() {
    this.note = { title: '', content: '' };
  }
}