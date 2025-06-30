import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { PrestamoService, Prestamo } from '../../services/prestamo.service';
import { NotificationService } from '../../services/notification.service';
import { ViewEncapsulation } from '@angular/core';

@Component({
  selector: 'app-nuevo-prestamo',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './nuevo-prestamo.component.html',
  styleUrls: ['./nuevo-prestamo.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class NuevoPrestamoComponent {
  prestamo: Prestamo = {
    monto: 0,
    tasaInteresMensual: 0,
    numeroCuotas: 0,
    fechaInicio: ''
  };
  clienteId!: number;

  constructor(
    private prestamoService: PrestamoService,
    private route: ActivatedRoute,
    private router: Router,
    private notificationService: NotificationService
  ) {}

  ngOnInit() {
    this.clienteId = Number(this.route.snapshot.paramMap.get('clienteId'));
  }

  guardarPrestamo() {
    this.prestamoService.crearPrestamo(this.clienteId, this.prestamo).subscribe({
      next: () => {
        this.notificationService.show('success', 'Préstamo creado correctamente.');
        this.router.navigate(['/clientes/prestamos', this.clienteId]);
      },
      error: () => {
        this.notificationService.show('error', 'Hubo un error al crear el préstamo.');
      }
    });
  }

  cancelar() {
    this.router.navigate(['/clientes/prestamos', this.clienteId]);
  }
}
