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
  yAxisLabel = 'Cantidad';

  colorScheme: any = {
    name: 'custom',
    selectable: true,
    group: 'Ordinal',
    domain: ['#7aa3e5', '#a8385d', '#aae3f5', '#A58ABF', '#E3B075', '#FFF293', '#C9DC92', '#E07C3E', '#FFF8DC', '#8B0000', '#FFFAF0', '#E0FFFF']
  };

  selectedData: any = null;
  showModal: boolean = false;

  constructor(private reportService: ReportGraficoService) {}

  ngOnInit(): void {
      this.adjustChartView();
      this.reportService.getPorAnioConMeses().subscribe(data => {
        this.anualConMesesData = this.mapAnualConMesesToChart(data);

        // Extraer los años y ordenarlos decrecientemente
        this.availableYears = this.anualConMesesData
          .map(item => item.anio)
          .sort((a, b) => +b - +a);

        // Seleccionar el año más reciente por defecto
        this.selectedYear = this.availableYears[0];
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
        chartData: meses.map((m: any) => ({
          name: m.mes,
          value: m.montoPagado,  // Lo que la barra muestra: monto pagado
          extra: {
            montoPrestado: m.montoPrestado,
            montoPagado: m.montoPagado,
            anio: anio
          }
        }))
      };
    });
  }

  onBarSelect(event: any): void {
    this.selectedData = event;
    this.showModal = true;
  }

  changeYear(year: string): void {
    this.selectedYear = year;
  }
}
