import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, UserDto, RegisterDto } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';
import { ViewEncapsulation } from '@angular/core';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './auth.component.html',
  styleUrls: ['./auth.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class AuthComponent {
  isLoginMode: boolean = true;
  loginData: UserDto = { username: '', password: '' };
  registerData: RegisterDto = { username: '', password: '', whatsapp: '' };
  errorMessage: string = '';
  passwordTouched: boolean = false;
  passwordValid: boolean = false;

  constructor(private authService: AuthService, private router: Router, private notificationService: NotificationService) {}

  toggleMode(): void {
    this.isLoginMode = !this.isLoginMode;
    this.errorMessage = '';
    this.passwordTouched = false;
    this.passwordValid = false;
  }

  validatePassword(): void {
      const pwd = this.registerData.password;
      // Validación: min 8, letras y números
      this.passwordValid = !!pwd && pwd.length >= 8 && /[A-Za-z]/.test(pwd) && /\d/.test(pwd);
    }

  onSubmit(): void {
      if (this.isLoginMode) {
        this.authService.login(this.loginData).subscribe({
          next: () => {
            this.notificationService.show('success', 'Usted a iniciado sesión exitosamente.');
            this.router.navigate(['/clientes']);
          },
          error: () => this.notificationService.show('error', 'Usuario o contraseña incorrectos.'),
        });
      } else {
        this.validatePassword();
        if (!this.passwordValid) {
          this.notificationService.show('error', 'Revice el requisito de la contraseña.');
          return; // No deja registrar
        }

        this.authService.register(this.registerData).subscribe({
          next: () => {
            this.notificationService.show('success', 'Usted se a registrado con exitosamente.');
            this.isLoginMode = true;
          },
          error: () => this.notificationService.show('error', 'Hubo un error al registrarse.'),
        });
      }
    }

  showPasswordLogin: boolean = false;
  showPasswordRegister: boolean = false;

  togglePasswordVisibility(tipo: 'login' | 'register'): void {
    if (tipo === 'login') {
      this.showPasswordLogin = !this.showPasswordLogin;
    } else {
      this.showPasswordRegister = !this.showPasswordRegister;
    }
  }
}
