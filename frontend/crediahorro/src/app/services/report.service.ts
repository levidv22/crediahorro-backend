import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private baseUrl = `${environment.apiUrl}/report-ms/report`;

  constructor(private http: HttpClient) {}

  generarReporte(nombre: string, tipo: string, estado: string): Observable<string> {
    const params = new HttpParams()
      .set('nombre', nombre)
      .set('tipo', tipo)
      .set('estado', estado);

    return this.http.get(this.baseUrl + '/generar', { params, responseType: 'text' });
  }

  buscarClientes(nombre: string): Observable<string[]> {
    const params = new HttpParams().set('nombre', nombre);
    return this.http.get<string[]>(`${environment.apiUrl}/admin-service/clientes/buscar`, { params });
  }
}
