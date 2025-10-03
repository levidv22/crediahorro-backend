import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';

export interface Cuota {
  id: number;
  fechaPago: string;
  montoCuota: number;
  capital: number;
  interes: number;
  estado: string;
  tipoPago: string;
  fechaPagada: string;
}

export interface Prestamo {
  id?: number;
  tipoCuota?: string;
  cuotas?: Cuota[];
}

export interface PrestamoCuotasResponse {
  prestamoId: number;
  tipoCuota: string;
  cuotas: Cuota[];
  cuotasPendientes: number;
}

@Injectable({
  providedIn: 'root'
})
export class CuotaService {
  private baseUrl = `${environment.apiUrl}/admin-service/cuotas`;

  constructor(private http: HttpClient) {}

  getCuotasByPrestamo(prestamoId: number): Observable<PrestamoCuotasResponse> {
    return this.http.get<PrestamoCuotasResponse>(`${this.baseUrl}/prestamo/${prestamoId}`);
  }

  pagarCuota(cuotaId: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${cuotaId}/pagar`, {});
  }

  getSaldosByPrestamo(prestamoId: number): Observable<{ capital: number; interes: number; total: number }> {
    return this.http.get<{ capital: number; interes: number; total: number }>(
      `${this.baseUrl}/prestamos/${prestamoId}/saldos`
    );
  }

  aplicarPagoAdelantado(prestamoId: number, monto: number, tipoPago: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/prestamos/${prestamoId}/pago-adelantado`, null, {
      params: {
        monto: monto.toString(),
        tipoPago
      }
    });
  }

  marcarCuotaNoPagada(cuotaId: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${cuotaId}/no-pagar`, {});
  }

  pagarCuotaParcial(cuotaId: number, monto: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${cuotaId}/pago-parcial`, null, {
      params: { monto: monto.toString() }
    });
  }
}
