import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AddNote } from '../../components/notes/add-note/add-note';
import { ListNotes } from '../../components/notes/list-notes/list-notes';
import { Auth } from '../../services/auth';
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, AddNote, ListNotes],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard {
  userName = '';
  userRole = '';
  notesCount = 0;
  constructor(private router: Router, private auth: Auth) {}
  ngOnInit() {
    console.log('🔍 DEBUG LOCALSTORAGE:');
    console.log('📍 localStorage length:', localStorage.length);
    console.log('📍 Todas las keys:', Object.keys(localStorage));
    console.log('📍 authToken value:', localStorage.getItem('auth_token'));
    console.log('📍 token value:', localStorage.getItem('token'));

    const token = localStorage.getItem('auth_token');
    console.log('TOKEN DESDE LOCALSTORAGE:', token);
    if (!token) {
      this.router.navigate(['/login']);
      return;
    }
    this.loadUserProfile();
  }
  loadUserProfile() {
    this.auth.me().subscribe({
      next: (data: any) => {
        this.userName = data.email;
        this.userRole = data.role;
      },
      error: () => {
        this.router.navigate(['/login']);
      },
    });
  }
  logout() {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('token');
    sessionStorage.removeItem('auth_token');
    window.location.href = '/login';
  }
  getUserInitials(): string {
    if (!this.userName) return 'U';
    return this.userName
      .split(' ')
      .map((n) => n[0])
      .join('')
      .toUpperCase()
      .substring(0, 2);
  }
  onNotesCountChange(count: number) {
    setTimeout(() => {
      this.notesCount = count;
    }, 0);
  }
}
