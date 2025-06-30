import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../services/notification.service';
import { ViewEncapsulation } from '@angular/core';

@Component({
  selector: 'app-alert',
  standalone: true,
  imports: [CommonModule],
  template: `
    <!-- alert.component.html -->
    <div
      class="custom-alert"
      [ngClass]="notification.tipo === 'success' ? 'bg-success' : 'bg-danger'"
      [class.show]="notification.visible"
      role="alert"
    >
      <i
        class="icon bi"
        [ngClass]="notification.tipo === 'success' ? 'bi-check-circle-fill' : 'bi-x-circle-fill'"
      ></i>
      <span>{{ notification.texto }}</span>
    </div>
  `,
  styleUrls: ['./alert.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class AlertComponent {
  notification = { tipo: 'success', texto: '', visible: false };
  constructor(private notificationService: NotificationService) {
    this.notificationService.notification$.subscribe(n => this.notification = n);
  }
}
