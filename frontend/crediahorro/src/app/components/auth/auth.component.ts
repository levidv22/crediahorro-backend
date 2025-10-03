import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, UserDto, RegisterDto } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';
import { ViewEncapsulation } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

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
  registerData: RegisterDto = { username: '', password: '', whatsapp: '', email: '' };
  errorMessage: string = '';
  passwordTouched: boolean = false;
  passwordValid: boolean = false;
  isLoading: boolean = false;

  constructor(
    private authService: AuthService,
    private http: HttpClient,
    private router: Router,
    private notificationService: NotificationService
  ) {}

  toggleMode(): void {
    this.isLoginMode = !this.isLoginMode;
    this.errorMessage = '';
    this.passwordTouched = false;
    this.passwordValid = false;
  }

  validatePassword(): void {
    const pwd = this.registerData.password;
    this.passwordValid = !!pwd && pwd.length >= 8 && /[A-Za-z]/.test(pwd) && /\d/.test(pwd);
  }

  onSubmit(): void {
    if (this.isLoginMode) {
      this.isLoading = true;

      this.authService.login(this.loginData).subscribe({
        next: () => {
          this.notificationService.show('success', 'Sesión iniciada.');
          this.router.navigate(['/clientes']); // Redirigir directo a clientes
          this.isLoading = false;
        },
        error: () => {
          this.isLoading = false;
          this.notificationService.show('error', 'Usuario o contraseña incorrectos.');
        },
      });
    } else {
      this.validatePassword();
      if (!this.passwordValid) {
        this.notificationService.show('error', 'Revise el requisito de la contraseña.');
        return;
      }

      this.isLoading = true;
      this.authService.register(this.registerData).subscribe({
        next: () => {
          this.notificationService.show('success', 'Registro exitoso.');
          this.isLoginMode = true;
          this.isLoading = false;
        },
        error: (err) => {
          console.error(err);
          this.notificationService.show('error', 'Hubo un error al registrarse.');
          this.isLoading = false;
        },
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
