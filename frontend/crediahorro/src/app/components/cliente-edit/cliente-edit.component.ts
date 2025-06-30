import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Cliente, Prestamo, ClienteService } from '../../services/cliente.service';
import { FormBuilder, FormGroup, Validators, FormArray, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NotificationService } from '../../services/notification.service';
import { ViewEncapsulation } from '@angular/core';

@Component({
  selector: 'app-cliente-edit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './cliente-edit.component.html',
  styleUrls: ['./cliente-edit.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class ClienteEditComponent implements OnInit {
  clienteForm!: FormGroup;
  clienteId!: number;
  clienteData!: Cliente;

  constructor(
    private route: ActivatedRoute,
    private clienteService: ClienteService,
    private fb: FormBuilder,
    private router: Router,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.clienteId = Number(this.route.snapshot.paramMap.get('id'));
    this.clienteService.getClienteById(this.clienteId).subscribe(cliente => {
      this.clienteData = cliente;
      this.initForm(cliente);
    });
  }

  initForm(cliente: Cliente) {
    this.clienteForm = this.fb.group({
      nombre: [cliente.nombre, Validators.required],
      dni: [cliente.dni, Validators.required],
      direccion: [cliente.direccion],
      telefonoWhatsapp: [cliente.telefonoWhatsapp],
      correoElectronico: [cliente.correoElectronico, [Validators.email]],
      prestamos: this.fb.array([])
    });

    // Inicializa préstamos existentes
    if (cliente.prestamos) {
      cliente.prestamos.forEach(prestamo => {
        this.prestamos.push(this.createPrestamoGroup(prestamo));
      });
    }
  }

  get prestamos(): FormArray {
    return this.clienteForm.get('prestamos') as FormArray;
  }

  createPrestamoGroup(prestamo?: Prestamo): FormGroup {
    return this.fb.group({
      monto: [prestamo ? prestamo.monto : 0, Validators.required],
      tasaInteresMensual: [prestamo ? prestamo.tasaInteresMensual : 0, Validators.required],
      numeroCuotas: [prestamo ? prestamo.numeroCuotas : 0, Validators.required],
      fechaInicio: [prestamo ? prestamo.fechaInicio : '', Validators.required]
    });
  }

  agregarPrestamo() {
    this.prestamos.push(this.createPrestamoGroup());
  }

  eliminarPrestamo(index: number) {
    this.prestamos.removeAt(index);
  }

  onSubmit() {
    const updatedCliente: Cliente = {
      ...this.clienteData,
      ...this.clienteForm.value
    };
    this.clienteService.updateCliente(this.clienteId, updatedCliente).subscribe({
      next: () => {
        this.notificationService.show('success', 'Cliente editado correctamente.');
        this.router.navigate(['/clientes']);
      },
      error: () => {
        this.notificationService.show('error', 'Hubo un error al editar el cliente.');
      }
    });
  }
}
