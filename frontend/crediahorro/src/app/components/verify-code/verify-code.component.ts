import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService, CodeDto } from '../../services/auth.service';
import { Router } from '@angular/router';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-verify-code',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './verify-code.component.html',
  styleUrls: ['./verify-code.component.css']
})
export class VerifyCodeComponent {
  accessCode: string = '';
  username: string | null = '';

  constructor(
    private authService: AuthService,
    private router: Router,
    private notificationService: NotificationService
  ) {
    this.username = this.authService.getUsername();
  }

  verify(): void {
    if (!this.username) {
      this.notificationService.show('error', 'No se encontró usuario.');
      return;
    }

    const dto: CodeDto = {
      username: this.username,
      accessCode: this.accessCode
    };

    this.authService.verifyCode(dto).subscribe({
      next: () => {
        this.notificationService.show('success', 'Código verificado. Sesión iniciada.');
        this.router.navigate(['/clientes']); // 🚩 Cambia a tu ruta principal
      },
      error: () => {
        this.notificationService.show('error', 'Código inválido.');
      }
    });
  }
}
