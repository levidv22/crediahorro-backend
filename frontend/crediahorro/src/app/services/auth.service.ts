import { Injectable, NgZone } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable, tap, BehaviorSubject } from 'rxjs';
import { NotificationService } from './notification.service';

export interface UserDto {
  username: string;
  password: string;
}

export interface RegisterDto {
  username: string;
  password: string;
  whatsapp: string;
  email?: string;
}

export interface TokenDto {
  accessToken: string;
}

export interface CodeDto {
  username: string;
  accessCode: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl = `${environment.apiUrl}/auth-service/auth`;

  private usernameSubject = new BehaviorSubject<string | null>(this.getUsername());
  username$ = this.usernameSubject.asObservable();

  private inactivityTimer: any;

  constructor(private http: HttpClient, private ngZone: NgZone, private notificationService: NotificationService) {
    this.initInactivityWatcher();
  }
  login(user: UserDto): Observable<TokenDto> {
    return this.http.post<TokenDto>(`${this.baseUrl}/login`, user).pipe(
      tap(res => {
        localStorage.setItem('accessToken', res.accessToken);
        localStorage.setItem('username', user.username);
        this.usernameSubject.next(user.username);
        this.resetInactivityTimer();
      })
    );
  }

  register(registerDto: RegisterDto): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/register`, registerDto);
  }

  verifyCode(codeDto: CodeDto): Observable<TokenDto> {
    // Eliminado porque ya no se usa
    return new Observable();
  }

  logout(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('username');
    this.usernameSubject.next(null);
    this.clearInactivityTimer();
  }

  getToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getUsername(): string | null {
    return localStorage.getItem('username');
  }

  private initInactivityWatcher(): void {
      this.ngZone.runOutsideAngular(() => {
        window.addEventListener('mousemove', () => this.resetInactivityTimer());
        window.addEventListener('keydown', () => this.resetInactivityTimer());
        window.addEventListener('click', () => this.resetInactivityTimer());
        this.resetInactivityTimer();
      });
    }

    private resetInactivityTimer(): void {
      this.clearInactivityTimer();

      if (this.isLoggedIn()) {
        this.inactivityTimer = setTimeout(() => {
          this.ngZone.run(() => {
            this.logout();
            this.notificationService.show('success', '⏰ Sesión cerrada por inactividad.');
            location.href = '/auth';
          });
        }, 15 * 60 * 1000); // 3 minutos
      }
    }

    private clearInactivityTimer(): void {
      if (this.inactivityTimer) {
        clearTimeout(this.inactivityTimer);
      }
    }
}
