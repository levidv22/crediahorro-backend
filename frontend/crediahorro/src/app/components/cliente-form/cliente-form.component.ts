import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Router } from '@angular/router';
import { ClienteService, Cliente, Prestamo } from '../../services/cliente.service';
import { NotificationService } from '../../services/notification.service';
import { ViewEncapsulation } from '@angular/core';

@Component({
  selector: 'app-cliente-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './cliente-form.component.html',
  styleUrls: ['./cliente-form.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class ClienteFormComponent {
  cliente: Cliente = {
    nombre: '',
    dni: '',
    direccion: '',
    telefonoWhatsapp: '',
    correoElectronico: '',
    prestamos: []  // importante inicializar
  };

  constructor(private clienteService: ClienteService, private router: Router, private notificationService: NotificationService) { }

  agregarPrestamo(): void {
    this.cliente.prestamos!.push({
      monto: 0,
      tasaInteresMensual: 0,
      numeroCuotas: 0,
      fechaInicio: ''
    });
  }

  eliminarPrestamo(index: number): void {
    this.cliente.prestamos!.splice(index, 1);
  }

  guardarCliente(): void {
    this.clienteService.crearCliente(this.cliente).subscribe({
      next: () => {
        this.notificationService.show('success', 'Cliente creado correctamente.');
        this.router.navigate(['/clientes']);
      },
      error: () => {
        this.notificationService.show('error', 'Hubo un error al crear el cliente.');
      }
    });
  }
}
