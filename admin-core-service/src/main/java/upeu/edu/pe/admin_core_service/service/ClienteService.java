package upeu.edu.pe.admin_core_service.service;

import upeu.edu.pe.admin_core_service.entities.Cliente;
import upeu.edu.pe.admin_core_service.entities.Prestamo;

import java.util.List;
import java.util.Optional;

public interface ClienteService {
    List<Cliente> obtenerTodosLosClientes();
    Cliente guardarCliente(Cliente cliente);
    List<Cliente> buscarClientesPorNombre(String nombreParcial);
    void generarCuotas(Prestamo prestamo);
    double calcularCuota(double monto, double tasa, int numeroCuotas);
    Optional<Cliente> obtenerClientePorId(Long id);
    Optional<Cliente> actualizarCliente(Long id, Cliente clienteActualizado);
    void eliminarCliente(Long id);
}