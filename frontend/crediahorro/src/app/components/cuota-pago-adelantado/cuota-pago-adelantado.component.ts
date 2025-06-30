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
  tipoReduccion: string = 'CUOTA';

  constructor(
    private cuotaService: CuotaService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.prestamoId = +this.route.snapshot.paramMap.get('prestamoId')!;
  }

  aplicarPagoAdelantado() {
    this.cuotaService.aplicarPagoAdelantado(this.prestamoId, this.monto, this.tipoReduccion).subscribe(() => {
      this.router.navigate(['/cuotas', this.prestamoId]);
    });
  }

  volver() {
    this.router.navigate(['/cuotas', this.prestamoId]);
  }
}
