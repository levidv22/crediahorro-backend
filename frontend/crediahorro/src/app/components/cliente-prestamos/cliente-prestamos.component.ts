import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Cliente, ClienteService } from '../../services/cliente.service';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ViewEncapsulation } from '@angular/core';

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
    private router: Router
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.clienteService.getClienteById(id).subscribe(cliente => {
      // Asegurar que prestamos siempre sea un array aunque no venga del backend
      if (!cliente.prestamos) {
        cliente.prestamos = [];
      }
      this.cliente = cliente;
    });
  }
}
