import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';

@Component({
  selector: 'app-list-notes',
  imports: [CommonModule],
  templateUrl: './list-notes.html',
  styleUrl: './list-notes.css',
})
export class ListNotes {
  notes = [
    { title: 'Primera Nota', content: 'Esta es una nota de ejemplo con contenido corto.' },
    { title: 'Segunda Nota', content: 'Otra nota para ver como se ve el diseño del listado.' }
  ]
}
