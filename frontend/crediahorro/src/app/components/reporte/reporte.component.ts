import { Component, OnInit } from '@angular/core';
import { ReportService } from '../../services/report.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ViewEncapsulation } from '@angular/core';

@Component({
  selector: 'app-reporte',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reporte.component.html',
  styleUrls: ['./reporte.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class ReporteComponent implements OnInit {
  nombre: string = '';
  tipo: string = 'prestamos';
  estado: string = 'PAGADO';
  estadosDisponibles: string[] = [];
  resultadoReporte: string | null = null;
  sugerencias: string[] = [];

  constructor(private reportService: ReportService) {}

  ngOnInit(): void {
    this.actualizarEstados();
  }

  actualizarEstados(): void {
    if (this.tipo === 'cuotas') {
      this.estadosDisponibles = ['PAGADA'];
      this.estado = this.estadosDisponibles[0];
    } else if (this.tipo === 'prestamos') {
      this.estadosDisponibles = ['PAGADO'];
      this.estado = this.estadosDisponibles[0];
    }
  }

  generarReporte(): void {
    if (!this.nombre || !this.tipo || !this.estado) {
      alert('Completa todos los campos.');
      return;
    }

    this.reportService.generarReporte(this.nombre, this.tipo, this.estado)
      .subscribe({
        next: (data) => {
          if (!data || data.trim() === '') {
            this.resultadoReporte = '<div class="alert alert-warning">No hay datos existentes según su petición.</div>';
          } else {
            this.resultadoReporte = data;
          }
        },
        error: (error) => {
          console.error(error);
          alert('Error al generar el reporte.');
        }
      });
  }

  buscarSugerencias(): void {
    if (this.nombre.length >= 2) { // solo buscar a partir de 2 letras
      this.reportService.buscarClientes(this.nombre).subscribe((nombres) => {
        this.sugerencias = nombres;
      });
    } else {
      this.sugerencias = [];
    }
  }

  seleccionarSugerencia(nombreSeleccionado: string): void {
    this.nombre = nombreSeleccionado;
    this.sugerencias = [];
  }
}
