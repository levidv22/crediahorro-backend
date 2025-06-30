import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Cliente } from '../../services/cliente.service';

@Component({
  selector: 'app-cliente-card',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './cliente-card.component.html',
  styleUrls: ['./cliente-card.component.css']
})
export class ClienteCardComponent {
  @Input() cliente!: Cliente;
}
