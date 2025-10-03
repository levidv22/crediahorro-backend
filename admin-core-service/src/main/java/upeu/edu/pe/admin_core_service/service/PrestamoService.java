package upeu.edu.pe.admin_core_service.service;

import upeu.edu.pe.admin_core_service.dto.ClientePagoDTO;
import upeu.edu.pe.admin_core_service.entities.Prestamo;

import java.util.List;
import java.util.Optional;

public interface PrestamoService {
    Optional<Prestamo> obtenerPrestamoPorId(Long id);
    List<Prestamo> obtenerTodos();
    Prestamo crearPrestamoParaCliente(Long clienteId, Prestamo prestamo);
    Prestamo actualizarPrestamo(Long id, Prestamo nuevoPrestamo);
    double calcularCuota(double monto, double tasa, int numeroCuotas);
    void eliminarPrestamo(Long id);
    List<ClientePagoDTO> obtenerResumenPagosPorAdministrador();
}
