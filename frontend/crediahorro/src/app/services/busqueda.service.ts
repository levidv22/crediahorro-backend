import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BusquedaService {
  private terminoBusqueda = new Subject<string>();
  terminoBusqueda$ = this.terminoBusqueda.asObservable();

  buscar(termino: string) {
    this.terminoBusqueda.next(termino);
  }
}
