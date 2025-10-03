import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CuotaService, Cuota, Prestamo, PrestamoCuotasResponse } from '../../services/cuota.service';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NotificationService } from '../../services/notification.service';
import { ViewEncapsulation } from '@angular/core';
import { ViewChild, ElementRef } from '@angular/core';
import { FormsModule } from '@angular/forms';

declare var bootstrap: any;

@Component({
  selector: 'app-cuota-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './cuota-list.component.html',
  styleUrls: ['./cuota-list.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class CuotaListComponent implements OnInit {
  cuotas: Cuota[] = [];
  prestamoId!: number;
  cuotasPendientes: number = 0;
  totalAPagar: number = 0;
  totalPagado: number = 0;
  faltaPagar: number = 0;
  mensajeVencimiento: string = '';
  prestamo: Prestamo | null = null;
  montoParcial: number = 0;
  cuotaIdSeleccionada: number | null = null;

  constructor(
    private cuotaService: CuotaService,
    private route: ActivatedRoute,
    private router: Router,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.prestamoId = +this.route.snapshot.paramMap.get('prestamoId')!;
    this.loadCuotas();
  }

  loadCuotas() {
    this.cuotaService.getCuotasByPrestamo(this.prestamoId).subscribe((data: PrestamoCuotasResponse) => {
        this.prestamo = {
          id: data.prestamoId,
          tipoCuota: data.tipoCuota,
          cuotas: data.cuotas
        };
        this.cuotas = this.prestamo.cuotas || [];
        this.cuotasPendientes = data.cuotasPendientes;

      this.totalAPagar = this.cuotas.reduce((sum, c) => sum + c.montoCuota, 0);
      this.totalPagado = this.cuotas
        .filter(c => c.estado === 'PAGADA')
        .reduce((sum, c) => sum + c.montoCuota, 0);
      this.faltaPagar = this.cuotas
        .filter(c => c.estado === 'PENDIENTE')
        .reduce((sum, c) => sum + c.montoCuota, 0);

      this.mensajeVencimiento = '';
      const hoy = new Date();
      hoy.setHours(0, 0, 0, 0); // Medianoche local

      // Ordenar cuotas por fechaPago
      const cuotasOrdenadas = [...this.cuotas].sort(
        (a, b) => new Date(a.fechaPago).getTime() - new Date(b.fechaPago).getTime()
      );

      // Cuota próxima a vencer
      const cuotaProxima = cuotasOrdenadas.find((cuota) => {
        if (cuota.estado !== 'PENDIENTE') return false;
        const fechaPagoDate = new Date(cuota.fechaPago + 'T00:00:00');
        fechaPagoDate.setHours(0, 0, 0, 0);
        const diffTime = fechaPagoDate.getTime() - hoy.getTime();
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        return diffDays >= 0 && diffDays <= 7;
      });

      if (cuotaProxima) {
        const fechaPagoDate = new Date(cuotaProxima.fechaPago + 'T00:00:00');
        fechaPagoDate.setHours(0, 0, 0, 0);
        const diffTime = fechaPagoDate.getTime() - hoy.getTime();
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        const index = cuotasOrdenadas.indexOf(cuotaProxima);
        this.mensajeVencimiento = diffDays === 0
          ? `Hoy vence la ${this.ordinal(index + 1)} cuota`
          : `Falta ${diffDays} día${diffDays === 1 ? '' : 's'} para vencerse la ${this.ordinal(index + 1)} cuota`;
      } else {
        // Cuota vencida
        const cuotaVencida = cuotasOrdenadas.find((cuota) => {
          if (cuota.estado !== 'PENDIENTE') return false;
          const fechaPagoDate = new Date(cuota.fechaPago + 'T00:00:00');
          fechaPagoDate.setHours(0, 0, 0, 0);
          return fechaPagoDate.getTime() < hoy.getTime();
        });

        if (cuotaVencida) {
          const fechaPagoDate = new Date(cuotaVencida.fechaPago + 'T00:00:00');
          fechaPagoDate.setHours(0, 0, 0, 0);
          const diffTime = hoy.getTime() - fechaPagoDate.getTime();
          const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));
          const index = cuotasOrdenadas.indexOf(cuotaVencida);
          this.mensajeVencimiento = `Venció la ${this.ordinal(index + 1)} cuota hace ${diffDays} día${diffDays === 1 ? '' : 's'}`;
        } else {
          const cuotasPagadas = cuotasOrdenadas.filter(c => c.estado === 'PAGADA');
          if (cuotasPagadas.length > 0) {
            const lastPagada = cuotasPagadas[cuotasPagadas.length - 1];
            const indexLastPagada = cuotasOrdenadas.indexOf(lastPagada);

            const hayPendientes = cuotasOrdenadas.some(c => c.estado === 'PENDIENTE');
            if (hayPendientes) {
              this.mensajeVencimiento = `${this.ordinal(indexLastPagada + 1)} cuota pagada`;
            }
          }
        }
      }
    });
  }

  // Método para ordinales en español
  ordinal(n: number): string {
    const ordinals = ['1ra', '2da', '3ra', '4ta', '5ta', '6ta', '7ma', '8va', '9na', '10ma',
      '11va', '12va', '13va', '14va', '15va', '16va', '17va', '18va', '19va', '20va',
      '21va', '22va', '23va', '24va', '25va', '26va', '27va', '28va', '29va', '30va',
      '31va', '32va', '33va', '34va', '35va', '36va', '37va', '38va', '39va', '40va',
      '41va', '42va', '43va', '44va', '45va', '46va', '47va', '48va', '49va', '50va',
      '51va', '52va', '53va', '54va', '55va', '56va', '57va', '58va', '59va', '60va'];
    return ordinals[n - 1] || `${n}ta`;
  }

  pagarCuota(cuotaId: number) {
    this.cuotaService.pagarCuota(cuotaId).subscribe(() => {
      this.loadCuotas();
    });
  }

  irPagoAdelantado() {
    this.router.navigate(['/cuotas', this.prestamoId, 'pago-adelantado']);
  }

  volver() {
    this.router.navigate(['/clientes']);
  }

  get mostrarBotonPagarAdelanto(): boolean {
    if (!this.cuotas || this.cuotas.length === 0) return false;

    const primeraCuota = this.cuotas[0];

    // Si la primera cuota está pagada, el botón debe ocultarse
    if (primeraCuota.estado === 'PAGADA') {
      return false;
    }

    // Verifica si ya hay un Adelanto Capital y un Adelanto Interés
    const tieneAdelantoCapital = this.cuotas.some(c =>
      c.tipoPago?.toLowerCase().includes('adelanto') &&
      c.tipoPago?.toLowerCase().includes('capital')
    );
    const tieneAdelantoInteres = this.cuotas.some(c =>
      c.tipoPago?.toLowerCase().includes('adelanto') &&
      c.tipoPago?.toLowerCase().includes('interés')
    );

    // Si ya se hicieron ambos pagos adelantados, oculta el botón
    if (tieneAdelantoCapital && tieneAdelantoInteres) {
      return false;
    }

    return true;
  }

  noPagarCuota(cuotaId: number) {
      this.cuotaService.marcarCuotaNoPagada(cuotaId).subscribe(() => {
        this.notificationService.show('success', 'Cuota reprogramada con éxito');
        this.loadCuotas();
      });
  }

  pagarCuotaParcial(cuotaId: number) {
    const input = prompt('Ingrese el monto que desea pagar parcialmente:');
    if (!input) return;

    const monto = parseFloat(input);
    if (isNaN(monto) || monto <= 0) {
      this.notificationService.show('error', 'Monto inválido');
      return;
    }

    this.cuotaService.pagarCuotaParcial(cuotaId, monto).subscribe(() => {
      this.notificationService.show('success', 'Pago parcial aplicado correctamente');
      this.loadCuotas();
    }, err => {
      this.notificationService.show('error', err.error?.message || 'Error al realizar el pago parcial');
    });
  }

  abrirModalPagoParcial(cuotaId: number) {
    this.cuotaIdSeleccionada = cuotaId;
    this.montoParcial = 0;

    const modal = new bootstrap.Modal(document.getElementById('modalPagoParcial'));
    modal.show();
  }

  confirmarPagoParcial() {
    if (!this.montoParcial || this.montoParcial <= 0) {
      this.notificationService.show('error', 'Monto inválido');
      return;
    }

    if (!this.cuotaIdSeleccionada) return;

    this.cuotaService.pagarCuotaParcial(this.cuotaIdSeleccionada, this.montoParcial).subscribe(() => {
      this.notificationService.show('success', 'Pago parcial aplicado correctamente');
      this.loadCuotas();

      const modal = bootstrap.Modal.getInstance(document.getElementById('modalPagoParcial'));
      modal.hide();
    }, err => {
      this.notificationService.show('error', err.error?.message || 'Error al realizar el pago parcial');
    });
  }

  esPagarHabilitado(index: number): boolean {
    // Siempre permitir pagar la última cuota si está pendiente
    const cuota = this.cuotas[index];
    if (cuota.estado === 'PENDIENTE' && index === this.cuotas.length - 1) {
      return true;
    }

    if (index === 0) return true;

    const cuotaAnterior = this.cuotas[index - 1];
    return (
      cuotaAnterior.estado === 'PAGADA' ||
      cuotaAnterior.estado === 'ADELANTADO' ||
      cuotaAnterior.tipoPago === 'PAGO_INCOMPLETO'
    );
  }

  esOtroHabilitado(index: number): boolean {
    // Desactivar si es la última cuota y todas las anteriores están pagadas
    const esUltimaCuota = index === this.cuotas.length - 1;
    const todasMenosUltimaPagadas = this.cuotas
      .slice(0, this.cuotas.length - 1)
      .every(c => c.estado === 'PAGADA');

    if (esUltimaCuota && todasMenosUltimaPagadas) {
      return false;
    }

    if (index === 0) return true;

    const cuotaAnterior = this.cuotas[index - 1];
    return (
      cuotaAnterior.estado === 'PAGADA' ||
      cuotaAnterior.estado === 'ADELANTADO' ||
      cuotaAnterior.tipoPago === 'PAGO_INCOMPLETO'
    );
  }
}
