import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { RouterModule, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { BusquedaService } from './services/busqueda.service';
import { AuthService } from './services/auth.service';
import { ClienteService } from './services/cliente.service';
import { NotificationService } from '../app/services/notification.service';
import { AlertComponent } from './components/alert/alert.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterModule, CommonModule, AlertComponent ],
  template: `
    <div class="d-flex flex-column min-vh-100">
      <!-- Navbar -->
      <nav *ngIf="!isAuthRoute()" class="navbar navbar-expand-lg navbar-dark bg-primary">
        <div class="container">
          <a class="navbar-brand" routerLink="/clientes">
            <i class="bi bi-cash-stack me-2"></i>CrediAhorro
          </a>
          <button
            class="navbar-toggler"
            type="button"
            data-bs-toggle="collapse"
            data-bs-target="#navbarNav"
            aria-controls="navbarNav"
            aria-expanded="false"
            aria-label="Toggle navigation"
          >
            <span class="navbar-toggler-icon"></span>
          </button>
          <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
              <li class="nav-item">
                <a class="nav-link" routerLink="/clientes" routerLinkActive="active">Clientes</a>
              </li>
              <li class="nav-item">
                <a class="nav-link" routerLink="/reportes" routerLinkActive="active">Historial Crediticio</a>
              </li>
              <li class="nav-item">
                <a class="nav-link" routerLink="/graficos" routerLinkActive="active">Gráficos</a>
              </li>
            </ul>

            <form class="d-flex me-3 gap-2" *ngIf="isClientesRoute()" (submit)="onBuscarCliente($event)">
              <div class="position-relative w-100">
                <input
                  class="form-control me-3"
                  id="buscadorInput"
                  type="search"
                  placeholder="Buscar Nombre"
                  aria-label="Search"
                  (input)="onInputCliente($event)" />

                <!-- Sugerencias -->
                <ul *ngIf="sugerenciasGlobales.length > 0"
                    class="list-group position-absolute mt-1 w-100 shadow"
                    style="z-index: 1000; cursor: pointer;">
                  <li *ngFor="let sug of sugerenciasGlobales"
                      class="list-group-item list-group-item-action"
                      (click)="seleccionarSugerenciaGlobal(sug)">
                    {{ sug }}
                  </li>
                </ul>
              </div>
              <button class="btn btn-outline-light" type="submit">
                <i class="bi bi-search"></i>
              </button>
            </form>


            <!-- Usuario en sesión -->
            <div *ngIf="username" class="text-light me-3">
              <i class="bi bi-person-circle me-1"></i>{{ username }}
            </div>
            <button *ngIf="username" class="btn btn-outline-light" (click)="cerrarSesion()" title="Cerrar Sesión">
              <i class="bi bi-box-arrow-right me-1"></i>
            </button>
          </div>
        </div>
      </nav>

      <!-- Content -->
      <main class="flex-fill container my-4">
        <app-alert></app-alert>
        <router-outlet></router-outlet>
      </main>

      <!-- Footer -->
      <footer *ngIf="!isAuthRoute()" class="bg-dark text-light py-3">
        <div class="container text-center">
          <p class="mb-1">&copy; 2025 CrediAhorro. Todos los derechos reservados.</p>
          <div class="d-flex justify-content-center">
            <a href="#" class="text-light mx-2"><i class="bi bi-facebook"></i></a>
            <a href="#" class="text-light mx-2"><i class="bi bi-instagram"></i></a>
            <a href="#" class="text-light mx-2"><i class="bi bi-linkedin"></i></a>
            <a href="#" class="text-light mx-2"><i class="bi bi-envelope-fill"></i></a>
          </div>
        </div>
      </footer>
    </div>
  `,
  styles: [`
    nav.navbar {
      background: linear-gradient(135deg, #004aad, #0069d9);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      transition: all 0.4s ease;
    }

    nav.navbar:hover {
      background: linear-gradient(135deg, #003d91, #005ac0);
    }

    .navbar-brand {
      font-weight: bold;
      font-size: 1.3rem;
      color: #fff !important;
      transition: transform 0.3s ease;
    }

    .navbar-brand:hover {
      transform: scale(1.05);
    }

    .navbar-nav .nav-link {
      color: #e6ecff !important;
      font-weight: 500;
      margin-right: 10px;
      transition: color 0.3s ease, transform 0.3s ease;
    }

    .navbar-nav .nav-link:hover {
      color: #ffffff !important;
      transform: translateY(-2px);
    }

    .navbar-nav .nav-link.active {
      color: #ffffff !important;
      border-bottom: 2px solid #ffffff;
    }

    .form-control:focus {
      border-color: #ffffff;
      box-shadow: 0 0 0 0.2rem rgba(255, 255, 255, 0.25);
    }

    .btn-outline-light {
      border-radius: 8px;
      font-weight: 500;
      transition: all 0.3s ease;
    }

    .btn-outline-light:hover {
      background-color: #ffffff;
      color: #004aad;
      border-color: #ffffff;
      transform: translateY(-1px);
    }

    .bi {
      vertical-align: middle;
    }

    footer {
      background: linear-gradient(135deg, #004aad, #002b5c);
      box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.2);
    }

    footer p {
      margin: 0;
      font-size: 0.9rem;
      color: #d6e0f5;
    }

    footer a {
      font-size: 1.2rem;
      transition: transform 0.3s ease, color 0.3s ease;
    }

    footer a:hover {
      color: #ffffff;
      transform: scale(1.2);
    }

    @media (max-width: 768px) {
      .navbar-nav .nav-link {
        margin: 8px 0;
        text-align: center;
      }

      .form-control {
        margin-bottom: 8px;
      }

      .navbar-collapse {
        background-color: rgba(0, 74, 173, 0.95);
        padding: 1rem;
        border-radius: 8px;
      }
    }
  `]
})
export class AppComponent implements OnInit {
  username: string | null = null;
  currentUrl: string = '';
  sugerenciasGlobales: string[] = [];

  constructor(
    private busquedaService: BusquedaService,
    private authService: AuthService,
    private router: Router,
    private notificationService: NotificationService,
    private clienteService: ClienteService
  ) {
    this.router.events.subscribe(() => {
      this.currentUrl = this.router.url;
    });
  }

  ngOnInit(): void {
    this.currentUrl = this.router.url;

    this.authService.username$.subscribe(nombre => {
      this.username = nombre;
    });
  }

  isClientesRoute(): boolean {
    return this.currentUrl === '/clientes';
  }

  isAuthRoute(): boolean {
    return this.currentUrl === '/auth';
  }

  onBuscarCliente(event: Event) {
    event.preventDefault();
    const input = (event.target as HTMLFormElement).querySelector<HTMLInputElement>('#buscadorInput');
    const value = input?.value.trim().toLowerCase();
    if (value) {
      this.busquedaService.buscar(value);
    }
  }

  cerrarSesion() {
    this.authService.logout();
    this.router.navigate(['/auth']);
    this.notificationService.show('success', 'Sesión cerrada con éxito.');
  }

  onInputCliente(event: Event) {
    const value = (event.target as HTMLInputElement).value.trim().toLowerCase();
    if (value.length >= 2) {
      this.clienteService.buscarClientes(value).subscribe(nombres => {
        this.sugerenciasGlobales = nombres;
      });
    } else {
      this.sugerenciasGlobales = [];
    }
  }

  seleccionarSugerenciaGlobal(nombre: string) {
    const input = document.getElementById('buscadorInput') as HTMLInputElement;
    input.value = nombre;
    this.sugerenciasGlobales = [];
    this.busquedaService.buscar(nombre.toLowerCase());
  }
}

