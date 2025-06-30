import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { PrestamoService, Prestamo } from '../../services/prestamo.service';
import { NotificationService } from '../../services/notification.service';
import { ViewEncapsulation } from '@angular/core';

@Component({
  selector: 'app-editar-prestamo',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './editar-prestamo.component.html',
  styleUrls: ['./editar-prestamo.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class EditarPrestamoComponent {
  prestamo!: Prestamo;
  clienteId!: number;
  prestamoId!: number;

  constructor(
    private prestamoService: PrestamoService,
    private route: ActivatedRoute,
    private router: Router,
    private notificationService: NotificationService
  ) {}

  ngOnInit() {
    this.prestamoId = Number(this.route.snapshot.paramMap.get('id'));
    this.clienteId = Number(this.route.snapshot.paramMap.get('clienteId'));

    this.prestamoService.getPrestamoById(this.prestamoId).subscribe({
      next: (data: Prestamo) => this.prestamo = data,
      error: (err: any) => console.error(err)
    });
  }

  actualizarPrestamo() {
    this.prestamoService.actualizarPrestamo(this.prestamoId, this.prestamo).subscribe({
      next: () => {
        this.notificationService.show('success', 'Préstamo editado correctamente.');
        this.router.navigate(['/clientes/prestamos', this.clienteId]);
      },
      error: () => {
        this.notificationService.show('error', 'Hubo un error al editar el préstamo.');
      }
    });
  }

  cancelar() {
    this.router.navigate(['/clientes/prestamos', this.clienteId]);
  }
}
