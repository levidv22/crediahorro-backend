import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface Notification {
  tipo: 'success' | 'error';
  texto: string;
  visible: boolean;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private notificationSubject = new BehaviorSubject<Notification>({ tipo: 'success', texto: '', visible: false });
  notification$ = this.notificationSubject.asObservable();

  show(tipo: 'success' | 'error', texto: string) {
    this.notificationSubject.next({ tipo, texto, visible: true });

    setTimeout(() => {
      this.notificationSubject.next({ tipo, texto: '', visible: false });
    }, 6000); // ocultar automáticamente a los 6s
  }
}
