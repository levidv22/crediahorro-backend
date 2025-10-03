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
    fechaInicio: '',
    fechaCreacion: '',
    tipoCuota: 'MENSUAL'
  };
  clienteId!: number;
  cuotasGeneradas: { fechaPago: string, montoCuota: number }[] = [];
  totalCuotas: number = 0;

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

  generarCuotas(prestamo: Prestamo): { fechaPago: string, montoCuota: number }[] {
      const cuotas = [];
      const monto = prestamo.monto!;
      const tasa = prestamo.tasaInteresMensual!;
      const numeroCuotas = prestamo.numeroCuotas!;
      const fechaInicio = new Date(prestamo.fechaInicio!);

      let interesTotal: number;
      let montoTotal: number;
      let montoCuota: number;

      if (prestamo.tipoCuota === 'DIARIO') {
        const meses = numeroCuotas / 30.0;
        interesTotal = monto * ((tasa * meses) / 100);
        montoTotal = monto + interesTotal;
      } else {
        interesTotal = monto * ((tasa * numeroCuotas) / 100);
        montoTotal = monto + interesTotal;
      }

      montoCuota = this.redondearConDecimalFinal0(montoTotal / numeroCuotas);

      for (let i = 0; i < numeroCuotas; i++) {
        const fecha = new Date(fechaInicio);
        if (prestamo.tipoCuota === 'DIARIO') {
          fecha.setDate(fecha.getDate() + i);
        } else {
          fecha.setMonth(fecha.getMonth() + i);
        }

        cuotas.push({
          fechaPago: fecha.toLocaleDateString('es-PE'),
          montoCuota: montoCuota
        });
      }

      return cuotas;
    }

    redondearConDecimalFinal0(valor: number): number {
      const redondeadoArriba = Math.ceil(valor * 10.0) / 10.0;
      return Math.round(redondeadoArriba * 10.0) / 10.0;
    }

    mostrarCuotas(prestamo: Prestamo): void {
      this.cuotasGeneradas = this.generarCuotas(prestamo);
      this.totalCuotas = this.cuotasGeneradas.reduce((acc, cuota) => acc + cuota.montoCuota, 0);
      const modal = new (window as any).bootstrap.Modal(document.getElementById('cuotasModal'));
      modal.show();
    }

    formatearFecha(fechaStr: string): string {
        const meses = [
          'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
          'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
        ];

        const partes = fechaStr.split(/[/-]/); // acepta tanto '/' como '-'
        const dia = parseInt(partes[0], 10);
        const mes = parseInt(partes[1], 10) - 1; // índice del mes
        const anio = parseInt(partes[2], 10);

        return `${dia} de ${meses[mes]} del ${anio}`;
      }
}
