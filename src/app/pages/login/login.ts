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

    const username = this.loginForm.value.username;
    const password = this.loginForm.value.password;

    console.log('Intentando login con:', { username, password });

    this.auth.login(username, password).subscribe({
      next: (res: any) => {
        this.loading = false;
        console.log('Respuesta completa del login:', res);

        if (res.token) {
          // Verificar que el token se guarde correctamente
          console.log('Token recibido:', res.token);
          this.auth.saveToken(res.token);

          // VERIFICAR que se guardó correctamente
          const savedToken = this.auth.getToken();
          console.log('Token guardado en localStorage:', savedToken);

          // Verificar que sean el mismo token
          if (res.token === savedToken) {
            console.log('✅ Tokens coinciden - todo correcto');
          } else {
            console.error('❌ ERROR: Tokens no coinciden');
            console.log('Token recibido:', res.token);
            console.log('Token guardado:', savedToken);
          }

          this.message = '✅ Login exitoso';

          setTimeout(() => {
            this.router.navigate(['/dashboard']);
          }, 1000);
        } else {
          this.message = '❌ No se recibió token del servidor';
        }
      },
      error: (err) => {
        this.loading = false;
        console.error('Error completo en login:', err);

        this.message =
          err.error?.message ||
          err.message ||
          (typeof err.error === 'string' ? err.error : 'Error al iniciar sesión');
      },
    });
  }
}
