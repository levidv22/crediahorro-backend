import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CuotaService } from '../../services/cuota.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ViewEncapsulation } from '@angular/core';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-cuota-pago-adelantado',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cuota-pago-adelantado.component.html',
  styleUrls: ['./cuota-pago-adelantado.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class CuotaPagoAdelantadoComponent implements OnInit {
  prestamoId!: number;
  monto: number = 0;
  tipoPago: string = 'CAPITAL';
  saldoCapital: number = 0;
  saldoInteres: number = 0;
  saldoTotal: number = 0;
  opcionesDisponibles: string[] = [];
  montos: { [tipo: string]: number } = {};

  constructor(
    private cuotaService: CuotaService,
    private route: ActivatedRoute,
    private router: Router,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.prestamoId = +this.route.snapshot.paramMap.get('prestamoId')!;
    this.cargarSaldos();

    const ultimoPago = localStorage.getItem(`ultimoTipoPago-${this.prestamoId}`);
      this.opcionesDisponibles = ['CAPITAL', 'INTERES', 'COMPLETO', 'MIXTO'];
      this.tipoPago = null!;
  }

  cargarSaldos() {
    this.cuotaService.getSaldosByPrestamo(this.prestamoId).subscribe({
      next: data => {
        this.saldoCapital = data.capital;
        this.saldoInteres = data.interes;
        this.saldoTotal = data.total;

        this.montos = {
          'CAPITAL': this.saldoCapital,
          'INTERES': this.saldoInteres,
          'COMPLETO': this.saldoTotal,
          'MIXTO': this.saldoInteres + 10 // sugerencia mínima
        };
      },
      error: err => {
        alert('Error al cargar saldos: ' + err.message);
      }
    });
  }

  actualizarMonto() {
    switch (this.tipoPago) {
      case 'CAPITAL':
        this.monto = this.saldoCapital;
        break;
      case 'INTERES':
        this.monto = this.saldoInteres;
        break;
      case 'COMPLETO':
        this.monto = this.saldoTotal;
        break;
      case 'MIXTO':
        // ⚠️ Aquí el monto se debe dejar libre o poner un mínimo sugerido
        this.monto = this.saldoInteres + 10; // sugerencia mínima
        break;
      default:
        this.monto = 0;
    }
  }

  aplicarPagoAdelantado(tipo: string) {
    const monto = this.montos[tipo];

    if (tipo === 'MIXTO' && monto < this.saldoInteres) {
      this.notificationService.show('error', 'El monto debe cubrir al menos el interés pendiente.');
      return;
    }

    this.cuotaService.aplicarPagoAdelantado(this.prestamoId, monto, tipo).subscribe({
      next: () => {
        if (tipo === 'CAPITAL' || tipo === 'INTERES') {
          localStorage.setItem(`ultimoTipoPago-${this.prestamoId}`, tipo);
        } else {
          localStorage.removeItem(`ultimoTipoPago-${this.prestamoId}`);
        }

        this.notificationService.show('success', '✅ ¡Pago adelantado aplicado correctamente!');
        this.router.navigate(['/cuotas', this.prestamoId]);
      },
      error: err => {
        this.notificationService.show('error', `❌ Error al aplicar pago adelantado: ${err.message}`);
      }
    });
  }

  volver() {
    this.router.navigate(['/cuotas', this.prestamoId]);
  }
}
