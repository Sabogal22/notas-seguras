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
  userRole = 'ADMIN';
  notesCount = 0;

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

  getUserInitials(): string {
    if (!this.userName) return 'U';
    return this.userName
      .split(' ')
      .map(name => name[0])
      .join('')
      .toUpperCase()
      .substring(0, 2);
  }

  onNotesCountChange(count: number) {
    this.notesCount = count;
  }
}