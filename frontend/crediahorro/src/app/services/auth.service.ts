import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable, tap, BehaviorSubject } from 'rxjs';

export interface UserDto {
  username: string;
  password: string;
}

export interface RegisterDto {
  username: string;
  password: string;
  whatsapp: string;
}

export interface TokenDto {
  accessToken: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl = `${environment.apiUrl}/auth-service/auth`;

  private usernameSubject = new BehaviorSubject<string | null>(this.getUsername());
  username$ = this.usernameSubject.asObservable();

  constructor(private http: HttpClient) {}

  login(user: UserDto): Observable<TokenDto> {
    return this.http.post<TokenDto>(`${this.baseUrl}/login`, user)
      .pipe(
        tap(res => {
          localStorage.setItem('accessToken', res.accessToken);
          localStorage.setItem('username', user.username);
          this.usernameSubject.next(user.username);
        })
      );
  }

  register(registerDto: RegisterDto): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/register`, registerDto);
  }

  logout(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('username');
    this.usernameSubject.next(null);
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
}
