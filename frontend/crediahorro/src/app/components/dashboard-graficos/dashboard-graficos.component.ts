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
  capitalInteresAdmin: any[] = [];
  pagosPorAdmin: any[] = [];
  toggleEstados: { [admin: string]: boolean } = {};
  selectedYear: string = '';
  showRecaudoPanel: boolean = false;

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
      this.anualConMesesData = this.mapIntereses(data);
      this.anualPrestamosData = this.mapPrestamos(data);
      this.availableYears = this.anualConMesesData.map(item => item.anio).sort((a, b) => +b - +a);
      this.selectedYear = this.availableYears[0];
    });

    this.reportService.getCapitalInteresPorAdmin().subscribe(data => {
      this.capitalInteresAdmin = this.mapCapitalInteresPorAdmin(data);
    });

    this.reportService.getPagosClientePorAdmin().subscribe(data => {
      this.pagosPorAdmin = data;
    });
  }

  mapCapitalInteresPorAdmin(data: any): any[] {
    const lista = [];
    for (const admin in data) {
      for (const anio in data[admin]) {
        lista.push({
          admin,
          anio,
          capital: data[admin][anio]["capital"] || 0,
          interes: data[admin][anio]["interes"] || 0
        });
      }
    }
    return lista;
  }

  get capitalInteresFiltrado(): any[] {
    return this.capitalInteresAdmin.filter(p => p.anio === this.selectedYear);
  }

  get totalCapitalInteres() {
    const totalCapital = this.capitalInteresFiltrado.reduce((sum, p) => sum + p.capital, 0);
    const totalInteres = this.capitalInteresFiltrado.reduce((sum, p) => sum + p.interes, 0);
    return { capital: totalCapital, interes: totalInteres };
  }

  get pagosPorAdminFiltrado(): any[] {
    return this.pagosPorAdmin.filter(p => {
      return p.fechaUltimaCuotaPagada?.startsWith(this.selectedYear);
    });
  }

  // Agrupamiento de clientes por socio (admin)
  get pagosAgrupadosPorSocio(): any[] {
    const agrupados: { [key: string]: any[] } = {};

    this.pagosPorAdminFiltrado.forEach(pago => {
      if (!agrupados[pago.usernameAdministrador]) {
        agrupados[pago.usernameAdministrador] = [];
      }
      agrupados[pago.usernameAdministrador].push(pago);
    });

    return Object.entries(agrupados).map(([admin, clientes]) => ({
      admin,
      clientes,
      abierto: this.toggleEstados[admin] || false
    }));
  }

  toggleDetalles(admin: string): void {
    this.toggleEstados[admin] = !this.toggleEstados[admin];
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

  get totalCapital(): number {
    const data = this.anualPrestamosData.find(d => d.anio === this.selectedYear);
    return data?.totalPrestamos || 0;
  }

  get totalInteres(): number {
    const data = this.anualConMesesData.find(d => d.anio === this.selectedYear);
    return data?.totalIntereses || 0;
  }

  get totalIngresos(): number {
    return this.totalCapital + this.totalInteres;
  }
}
