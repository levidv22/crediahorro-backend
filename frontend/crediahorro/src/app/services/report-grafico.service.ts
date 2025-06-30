import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ReportGraficoService {
  private apiUrl = 'http://localhost:4040/report-grafico/grafico';

  constructor(private http: HttpClient) {}

  getPorAnioConMeses(): Observable<any> {
    return this.http.get(`${this.apiUrl}/prestamos-por-anio`);
  }
}
