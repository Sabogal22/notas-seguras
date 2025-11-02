import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class Auth {
  private apiurl = 'http://localhost:8080/auth';

  constructor(private http: HttpClient) { }

  register(data: any): Observable<any> {
    return this.http.post(`${this.apiurl}/register`, data, { responseType: 'text' });
  }

  login(credentials: any): Observable<any> {
    return this.http.post(`${this.apiurl}/login`, credentials);
  }

  me(): Observable<any> {
    return this.http.get(`${this.apiurl}/me`);
  }

  // 👇 Agregamos esto para manejar el JWT
  saveToken(token: string): void {
    localStorage.setItem('token', token);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  logout(): void {
    localStorage.removeItem('token');
  }
}
