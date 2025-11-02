import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Auth } from '../../services/auth';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './register.html',
  styleUrls: ['./register.css']
})
export class Register {
  registerForm: FormGroup;
  message: string = '';

  constructor(private fb: FormBuilder, private auth: Auth) {
    this.registerForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(10)]],
      admin: [false]
    });
  }

    onSubmit() {
    if (this.registerForm.valid) {
      this.auth.register(this.registerForm.value).subscribe({
        next: (res) => {
          this.message = '✅ Usuario registrado correctamente';
          console.log(res);
        },
        error: (err) => {
          this.message = '❌ Error: ' + err.error;
          console.error(err);
        }
      });
    } else {
      this.message = '❌ El formulario no es válido';
    }
  }
}
