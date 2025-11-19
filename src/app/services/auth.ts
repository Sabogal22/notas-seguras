import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, tap, catchError, throwError } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class Auth {
  private apiUrl = 'http://localhost:8080/auth';

  constructor(private http: HttpClient) {}

  register(data: any): Observable<any> {
    return this.http
      .post(`${this.apiUrl}/register`, data, {
        responseType: 'text',
      })
      .pipe(catchError(this.handleError));
  }

  // En tu auth.service.ts - agrega este método
  clearOldTokens(): void {
    // Remover tokens viejos que puedan causar conflicto
    localStorage.removeItem('auth_token');
    localStorage.removeItem('token'); // Por si acaso hay otro key
    console.log('🧹 Tokens viejos limpiados');
  }

  // Y modifica el login para limpiar antes de guardar
  login(username: string, password: string): Observable<any> {
    if (!username || !password) {
      return throwError(() => new Error('Username and password are required'));
    }

    // LIMPIAR TOKENS VIEJOS ANTES DEL LOGIN
    this.clearOldTokens();

    const params = `?username=${encodeURIComponent(username)}&password=${encodeURIComponent(
      password
    )}`;
    console.log('URL de login:', `${this.apiUrl}/login${params}`);

    return this.http.post(`${this.apiUrl}/login${params}`, {}).pipe(
      tap((response: any) => {
        console.log('✅ Respuesta del servidor:', response);
        if (response && response.token) {
          this.saveToken(response.token);
          console.log('✅ Token guardado correctamente');
        } else {
          console.error('❌ No se recibió token en la respuesta');
        }
      }),
      catchError(this.handleError)
    );
  }

  me(): Observable<any> {
    if (!this.isAuthenticated()) {
      return throwError(() => new Error('No authentication token'));
    }

    return this.http
      .get(`${this.apiUrl}/me`, this.getAuthHeaders())
      .pipe(catchError(this.handleError));
  }

  logout(): Observable<any> {
    if (!this.isAuthenticated()) {
      this.removeToken();
      return throwError(() => new Error('No authentication token'));
    }

    return this.http.post(`${this.apiUrl}/logout`, {}, this.getAuthHeaders()).pipe(
      tap(() => {
        this.removeToken();
      }),
      catchError(this.handleError)
    );
  }

  // Métodos para manejar el JWT
  saveToken(token: string): void {
    if (token) {
      localStorage.setItem('auth_token', token);
      console.log('Token guardado');
    }
  }

  getToken(): string | null {
    const token = localStorage.getItem('auth_token');
    return token;
  }

  removeToken(): void {
    localStorage.removeItem('auth_token');
    console.log('Token removido');
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    return !!token; // Convierte a boolean
  }

  // Headers para requests autenticados
  getAuthHeaders() {
    const token = this.getToken();

    if (!token) {
      throw new Error('No authentication token available');
    }

    return {
      headers: new HttpHeaders({
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      }),
    };
  }

  // Manejo de errores
  private handleError(error: any) {
    console.error('Error en Auth Service:', error);

    let errorMessage = 'Error desconocido';
    if (error.error instanceof ErrorEvent) {
      // Error del cliente
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Error del servidor
      errorMessage = `Error ${error.status}: ${error.error || error.message}`;
    }

    return throwError(() => new Error(errorMessage));
  }
}
