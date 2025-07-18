import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ReportGraficoService {
  //private apiUrl = 'http://localhost:4040/report-grafico/grafico';
  private baseUrl = `${environment.apiUrl}/report-grafico/grafico`;

  constructor(private http: HttpClient) {}

  getPorAnioConMeses(): Observable<any> {
    return this.http.get(`${this.baseUrl}/prestamos-por-anio`);
  }

  getCapitalInteresPorAdmin(): Observable<any> {
    return this.http.get(`${this.baseUrl}/capital-interes-por-admin`);
  }

  getPagosClientePorAdmin(): Observable<any> {
    return this.http.get(`${this.baseUrl}/pagos-cliente-por-admin`);
  }
}
