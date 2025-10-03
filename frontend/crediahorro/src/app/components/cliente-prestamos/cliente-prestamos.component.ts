import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Cliente, ClienteService } from '../../services/cliente.service';
import { PrestamoService } from '../../services/prestamo.service';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ViewEncapsulation } from '@angular/core';
import { saveAs } from 'file-saver';

@Component({
  selector: 'app-cliente-prestamos',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './cliente-prestamos.component.html',
  styleUrls: ['./cliente-prestamos.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class ClientePrestamosComponent implements OnInit {
  cliente: Cliente = {
    id: 0,
    nombre: '',
    dni: '',
    direccion: '',
    telefonoWhatsapp: '',
    correoElectronico: '',
    prestamos: []
  };

  constructor(
    private route: ActivatedRoute,
    private clienteService: ClienteService,
    private prestamoService: PrestamoService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.clienteService.getClienteById(id).subscribe(cliente => {
      if (cliente.prestamos) {
        cliente.prestamos.forEach(prestamo => {
          if (prestamo.cuotas?.length) {
            prestamo.cuotas.sort((a, b) => new Date(a.fechaPago).getTime() - new Date(b.fechaPago).getTime());
          }
        });
        // Ordenar los préstamos: ACTIVO primero, luego PAGADO, luego otros
        cliente.prestamos.sort((a, b) => {
          const ordenEstado = (estado: string) => {
            switch (estado?.toUpperCase()) {
              case 'ACTIVO': return 0;
              case 'PAGADO': return 1;
              default: return 2;
            }
          };
          return ordenEstado(a.estado!) - ordenEstado(b.estado!);
        });
      } else {
        cliente.prestamos = [];
      }
      this.cliente = cliente;
    });
  }

  exportarExcel(prestamoId: number): void {
        this.prestamoService.exportarPrestamoPagado(prestamoId).subscribe(blob => {
          const filename = `prestamo-${prestamoId}.xlsx`;
          saveAs(blob, filename);
        });
      }
}
