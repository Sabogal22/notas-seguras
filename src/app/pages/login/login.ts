import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, FormGroup } from '@angular/forms';
import { Auth } from '../../services/auth';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css'],
})
export class Login implements OnInit {
  loginForm!: FormGroup;
  message = '';
  loading = false;

  constructor(private fb: FormBuilder, private auth: Auth, private router: Router) {}

  ngOnInit() {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
    });
  }

  onSubmit() {
    if (!this.loginForm.valid) {
      this.message = 'Completa todos los campos.';
      return;
    }

    this.loading = true;
    this.message = '';

    // Obtener los valores del formulario
    const username = this.loginForm.value.username;
    const password = this.loginForm.value.password;

    // CORRECCIÓN: Pasar los parámetros individualmente
    this.auth.login(username, password).subscribe({
      next: (res: any) => {
        this.loading = false;
        console.log('Respuesta del login:', res);
        
        if (res.token) {
          // Usar el método saveToken del servicio en lugar de localStorage directamente
          this.auth.saveToken(res.token);
          if (res.role) {
            localStorage.setItem('role', String(res.role));
          }
          this.message = '✅ Login exitoso';
          this.router.navigate(['/dashboard']);
        } else {
          this.message = 'Respuesta inesperada del servidor';
        }
      },
      error: (err) => {
        this.loading = false;
        this.message =
          err.error?.message ||
          (typeof err.error === 'string' ? err.error : 'Error al iniciar sesión');
        console.error('Error en login:', err);
      },
    });
  }
}