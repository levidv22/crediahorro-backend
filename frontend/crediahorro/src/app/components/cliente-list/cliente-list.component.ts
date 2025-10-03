import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ClienteService, Cliente } from '../../services/cliente.service';
import { BusquedaService } from '../../services/busqueda.service';
import { Subscription } from 'rxjs';
import { NotificationService } from '../../services/notification.service';
import { ClienteCardComponent } from '../cliente-card/cliente-card.component';
import { ViewEncapsulation } from '@angular/core';

@Component({
  selector: 'app-cliente-list',
  standalone: true,
  imports: [CommonModule, RouterModule, ClienteCardComponent],
  templateUrl: './cliente-list.component.html',
  styleUrls: ['./cliente-list.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class ClienteListComponent implements OnInit, OnDestroy {
  clientesConAlertas: Cliente[] = [];
  clientesBusqueda: Cliente[] = [];
  todosLosClientes: Cliente[] = [];
  busquedaSubscription!: Subscription;
  mostrarTablaTodos: boolean = false;
  paginaActual: number = 1;
  clientesPorPagina: number = 5;

  constructor(
    private clienteService: ClienteService,
    private busquedaService: BusquedaService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.cargarClientes();

    this.busquedaSubscription = this.busquedaService.terminoBusqueda$.subscribe(
      termino => this.buscarCliente(termino)
    );
  }

  ngOnDestroy(): void {
    this.busquedaSubscription?.unsubscribe();
  }

  cargarClientes(): void {
      this.clienteService.getClientes().subscribe(
        data => {
          const hoyDate = new Date();
          hoyDate.setHours(0, 0, 0, 0);

          const clientesConAlerta: Cliente[] = [];

          data.reverse().forEach(cliente => {
            this.procesarCliente(cliente, hoyDate);

            if (
              cliente.cuotaPendienteTexto &&
              (
                cliente.cuotaPendienteTexto.startsWith('Hoy vence') ||
                cliente.cuotaPendienteTexto.startsWith('En ') ||
                cliente.cuotaPendienteTexto.startsWith('La ')
              )
            ) {
              clientesConAlerta.push(cliente);
            }
          });

          this.todosLosClientes = data;
          this.clientesConAlertas = clientesConAlerta;
          this.clientesBusqueda = [];
        },
        error => console.error(error)
      );
    }

    buscarCliente(searchValue: string): void {
      const valor = searchValue.trim().toLowerCase();

      if (!valor) {
        this.clientesBusqueda = [];
        this.notificationService.show('success', 'Búsqueda limpiada.'); // Cambiado de 'info' a 'success'
        return;
      }

      const coincidencias = this.todosLosClientes.filter(cliente =>
        cliente.nombre.toLowerCase().includes(valor) ||
        cliente.dni.toLowerCase().includes(valor)
      );

      if (coincidencias.length === 0) {
        this.clientesBusqueda = [];
        this.notificationService.show('error', 'No se encontró ningún cliente con ese nombre.');
      } else {
        this.clientesBusqueda = coincidencias.map(c => this.procesarCliente(c));
        this.notificationService.show('success', 'Cliente encontrado correctamente.');
      }
    }

  ordinal(n: number): string {
    const ordinals = ['1ra', '2da', '3ra', '4ta', '5ta', '6ta', '7ma', '8va', '9na', '10ma',
      '11va', '12va', '13va', '14va', '15va', '16va', '17va', '18va', '19va', '20va',
      '21va', '22va', '23va', '24va', '25va', '26va', '27va', '28va', '29va', '30va',
      '31va', '32va', '33va', '34va', '35va', '36va', '37va', '38va', '39va', '40va',
      '41va', '42va', '43va', '44va', '45va', '46va', '47va', '48va', '49va', '50va',
      '51va', '52va', '53va', '54va', '55va', '56va', '57va', '58va', '59va', '60va'];
    return ordinals[n - 1] || `${n}ta`;
  }

  eliminarCliente(id: number): void {
    this.clienteService.eliminarCliente(id).subscribe(() => {
      this.notificationService.show('success', 'Cliente eliminado correctamente.');
      this.cargarClientes();
    }, () => {
      this.notificationService.show('error', 'Error eliminando cliente.');
    });
  }

  procesarCliente(cliente: Cliente, hoyDate = new Date()): Cliente {
    hoyDate.setHours(0, 0, 0, 0);
    cliente.cuotaPendienteTexto = '';
    cliente.estadoPrestamoMasReciente = 'SIN_PRESTAMO';

    if (!cliente.prestamos?.length) {
      return cliente;
    }

    // Ordenar del más reciente al más antiguo
    const prestamosOrdenados = [...cliente.prestamos].sort(
      (a, b) => new Date(b.fechaCreacion!).getTime() - new Date(a.fechaCreacion!).getTime()
    );

    // ✅ Buscar el préstamo ACTIVO más reciente
    const prestamoActivo = prestamosOrdenados.find(p => p.estado === 'ACTIVO');
    if (prestamoActivo) {
      cliente.estadoPrestamoMasReciente = 'ACTIVO';
    } else {
      cliente.estadoPrestamoMasReciente = prestamosOrdenados[0].estado;
    }

    // Buscar en todos los préstamos cuotas con alerta
    for (const prestamo of prestamosOrdenados) {
      if (!prestamo.cuotas?.length || prestamo.estado === 'PAGADO') continue;

      const cuotasOrdenadas = [...prestamo.cuotas].sort(
        (a, b) => new Date(a.fechaPago).getTime() - new Date(b.fechaPago).getTime()
      );

      const cuotaPendienteProxima = cuotasOrdenadas.find(cuota => {
        if (cuota.estado !== 'PENDIENTE') return false;
        const fecha = new Date(cuota.fechaPago + 'T00:00:00');
        const diff = fecha.getTime() - hoyDate.getTime();
        const dias = Math.ceil(diff / (1000 * 60 * 60 * 24));
        return dias >= 0 && dias <= 5;
      });

      if (cuotaPendienteProxima) {
        const fecha = new Date(cuotaPendienteProxima.fechaPago + 'T00:00:00');
        const diff = fecha.getTime() - hoyDate.getTime();
        const dias = Math.ceil(diff / (1000 * 60 * 60 * 24));
        const index = cuotasOrdenadas.indexOf(cuotaPendienteProxima);
        cliente.cuotaPendienteTexto = dias === 0
          ? `Hoy vence la ${this.ordinal(index + 1)} cuota`
          : `En ${dias} día${dias === 1 ? '' : 's'} se vence la ${this.ordinal(index + 1)} cuota`;
        return cliente;
      }

      const cuotaVencida = cuotasOrdenadas.find(cuota => {
        if (cuota.estado !== 'PENDIENTE') return false;
        const fecha = new Date(cuota.fechaPago + 'T00:00:00');
        return fecha.getTime() < hoyDate.getTime();
      });

      if (cuotaVencida) {
        const fecha = new Date(cuotaVencida.fechaPago + 'T00:00:00');
        const diff = hoyDate.getTime() - fecha.getTime();
        const dias = Math.floor(diff / (1000 * 60 * 60 * 24));
        const index = cuotasOrdenadas.indexOf(cuotaVencida);
        cliente.cuotaPendienteTexto = `La ${this.ordinal(index + 1)} cuota venció hace ${dias} día${dias === 1 ? '' : 's'}`;
        return cliente;
      }
    }

    // Si no hay cuotas pendientes, mostrar último pago
    const cuotasPagadas = prestamosOrdenados.flatMap(p => p.cuotas ?? []).filter(c => c.estado === 'PAGADA');
    if (cuotasPagadas.length > 0) {
      const index = cuotasPagadas.length;
      cliente.cuotaPendienteTexto = `Se pagó la ${this.ordinal(index)} cuota`;
    }

    return cliente;
  }

    get clientesPaginados(): Cliente[] {
        const inicio = (this.paginaActual - 1) * this.clientesPorPagina;
        const fin = inicio + this.clientesPorPagina;
        return this.todosLosClientes.slice(inicio, fin);
      }

      totalPaginas(): number {
        return Math.ceil(this.todosLosClientes.length / this.clientesPorPagina);
      }

      cambiarPagina(numero: number): void {
        if (numero >= 1 && numero <= this.totalPaginas()) {
          this.paginaActual = numero;
        }
      }

      toggleTablaTodos(): void {
        this.mostrarTablaTodos = !this.mostrarTablaTodos;
        this.paginaActual = 1; // Reinicia a la primera página
      }
}

