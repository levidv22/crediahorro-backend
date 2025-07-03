import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CuotaService } from '../../services/cuota.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ViewEncapsulation } from '@angular/core';

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

  constructor(
    private cuotaService: CuotaService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.prestamoId = +this.route.snapshot.paramMap.get('prestamoId')!;
    this.cargarSaldos();
  }

  cargarSaldos() {
      this.cuotaService.getSaldosByPrestamo(this.prestamoId).subscribe({
        next: data => {
          this.saldoCapital = data.capital;
          this.saldoInteres = data.interes;
          this.saldoTotal = data.total;

          // Inicializa el monto con capital por defecto
          this.actualizarMonto();
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
        default:
          this.monto = 0;
          break;
      }
    }

  aplicarPagoAdelantado() {
    this.cuotaService.aplicarPagoAdelantado(this.prestamoId, this.monto, this.tipoPago).subscribe({
      next: () => {
        alert('¡Pago adelantado aplicado correctamente!');
        this.router.navigate(['/cuotas', this.prestamoId]);
      },
      error: err => {
        alert('Error al aplicar pago adelantado: ' + err.message);
      }
    });
  }

  volver() {
    this.router.navigate(['/cuotas', this.prestamoId]);
  }
}
