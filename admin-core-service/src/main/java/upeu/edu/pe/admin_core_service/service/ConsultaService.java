package upeu.edu.pe.admin_core_service.service;

import upeu.edu.pe.admin_core_service.entities.Cuota;
import upeu.edu.pe.admin_core_service.entities.Prestamo;

import java.util.List;

public interface ConsultaService {
    List<Cuota> obtenerCuotasPorClienteYEstado(String nombre, String estado);
    List<Prestamo> obtenerPrestamosPorClienteYEstado(String nombre, String estado);
}
