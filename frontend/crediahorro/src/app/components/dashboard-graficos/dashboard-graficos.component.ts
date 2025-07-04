import { Component, OnInit, HostListener } from '@angular/core';
import { ReportGraficoService } from '../../services/report-grafico.service';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { FormsModule } from '@angular/forms';
import { ViewEncapsulation } from '@angular/core';

@Component({
  selector: 'app-dashboard-graficos',
  standalone: true,
  imports: [CommonModule, RouterModule, NgxChartsModule, FormsModule],
  templateUrl: './dashboard-graficos.component.html',
  styleUrls: ['./dashboard-graficos.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class DashboardGraficosComponent implements OnInit {
  anualConMesesData: any[] = [];
  anualPrestamosData: any[] = [];
  availableYears: string[] = [];
  selectedYear: string = '';

  view: [number, number] = [1200, 300];

  // opciones
  showXAxis = true;
  showYAxis = true;
  gradient = false;
  showLegend = true;
  showXAxisLabel = true;
  xAxisLabel = 'Periodo';
  showYAxisLabel = true;
  yAxisLabel = 'Intereses Pagados';

  // Esquema para INTERESES (NARANJA)
  colorSchemeIntereses: any = {
    name: 'intereses',
    selectable: true,
    group: 'Ordinal',
    domain: ['#FF9800']  // un naranja vibrante
  };

  // Esquema para PRESTAMOS (AZUL)
  colorSchemePrestamos: any = {
    name: 'prestamos',
    selectable: true,
    group: 'Ordinal',
    domain: ['#2196F3']  // un azul elegante
  };

  selectedData: any = null;
  modalTipo: string = '';
  showModal: boolean = false;

  constructor(private reportService: ReportGraficoService) {}

  ngOnInit(): void {
    this.adjustChartView();
    this.reportService.getPorAnioConMeses().subscribe(data => {
      // Mapea INTERESES
      this.anualConMesesData = this.mapIntereses(data);

      // Mapea PRESTAMOS
      this.anualPrestamosData = this.mapPrestamos(data);

      // Años disponibles
      this.availableYears = this.anualConMesesData
        .map(item => item.anio)
        .sort((a, b) => +b - +a);

      this.selectedYear = this.availableYears[0];
    });
  }

  mapIntereses(data: any): any[] {
    return Object.entries(data).map(([anio, mesesRaw]) => {
      const meses = mesesRaw as Array<any>;
      return {
        anio: anio,
        chartData: meses.filter(m => m.mes !== 'TOTAL')
          .map((m: any) => ({
            name: m.mes,
            value: m.interesPagado,
            extra: {
              interesPagado: m.interesPagado,
              anio: anio
            }
          })),
        totalIntereses: meses.find(m => m.mes === 'TOTAL')?.interesPagado || 0.0
      };
    });
  }

  mapPrestamos(data: any): any[] {
    return Object.entries(data).map(([anio, mesesRaw]) => {
      const meses = mesesRaw as Array<any>;
      return {
        anio: anio,
        chartData: meses.filter(m => m.mes !== 'TOTAL')
          .map((m: any) => ({
            name: m.mes,
            value: m.montoPrestado,
            extra: {
              montoPrestado: m.montoPrestado,
              anio: anio
            }
          })),
        totalPrestamos: meses.reduce((sum, m) => sum + (m.montoPrestado || 0), 0)
      };
    });
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    console.log(event.target.innerWidth);
    this.adjustChartView();
  }

  adjustChartView() {
    const width = window.innerWidth;
    if (width >= 1300) {
      this.view = [1300, 400];
    } else if (width >= 992) {
      this.view = [992, 400];
    } else if (width >= 768) {
      this.view = [768, 350];
    } else {
      this.view = [350, 300];
    }
  }

  mapAnualConMesesToChart(data: any): any[] {
    return Object.entries(data).map(([anio, mesesRaw]) => {
      const meses = mesesRaw as Array<any>;
      return {
        anio: anio,
        chartData: meses
          .filter(m => m.mes !== 'TOTAL') // ⚡ Excluye el TOTAL del gráfico
          .map((m: any) => ({
            name: m.mes,
            value: m.interesPagado,  // 👈 Ahora muestra interés
            extra: {
              interesPagado: m.interesPagado,
              anio: anio
            }
          })),
        totalIntereses: meses.find(m => m.mes === 'TOTAL')?.interesPagado || 0.0
      };
    });

    this.anualPrestamosData = Object.entries(data).map(([anio, mesesRaw]) => {
        const meses = mesesRaw as Array<any>;
        return {
          anio: anio,
          chartData: meses
            .filter(m => m.mes !== 'TOTAL')
            .map((m: any) => ({
              name: m.mes,
              value: m.montoPrestado,
              extra: {
                montoPrestado: m.montoPrestado,
                anio: anio
              }
            })),
          totalPrestamos: meses.find(m => m.mes === 'TOTAL')?.montoPrestado || 0.0
        };
      });
  }

  onBarSelect(event: any, tipo: 'interes' | 'prestamo'): void {
    this.selectedData = event;
    this.modalTipo = tipo;
    this.showModal = true;
  }

  changeYear(year: string): void {
    this.selectedYear = year;
  }
}
