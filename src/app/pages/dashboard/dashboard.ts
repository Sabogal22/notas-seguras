import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AddNote } from '../../components/notes/add-note/add-note';
import { ListNotes } from '../../components/notes/list-notes/list-notes';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, AddNote, ListNotes],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard {
  userName = 'Neythan Sabogal';

  /* funcion del login */
  /* constructor(private router: Router) {}

  ngOnInit () {
    const token = localStorage.getItem('token');
    if (!token) {
      this.router.navigate(['/login'])
    }
  } */

  logout() {
    localStorage.removeItem('token');
    window.location.href = '/login';
  }
}
