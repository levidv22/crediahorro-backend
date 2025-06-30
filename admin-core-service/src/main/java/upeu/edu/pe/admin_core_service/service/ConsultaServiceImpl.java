package upeu.edu.pe.admin_core_service.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import upeu.edu.pe.admin_core_service.entities.Cuota;
import upeu.edu.pe.admin_core_service.entities.Prestamo;
import upeu.edu.pe.admin_core_service.repository.CuotaRepository;
import upeu.edu.pe.admin_core_service.repository.PrestamoRepository;

import java.util.List;

@Service
@Transactional
public class ConsultaServiceImpl implements ConsultaService {

    private final PrestamoRepository prestamoRepository;
    private final CuotaRepository cuotaRepository;

    public ConsultaServiceImpl(PrestamoRepository prestamoRepository, CuotaRepository cuotaRepository) {
        this.prestamoRepository = prestamoRepository;
        this.cuotaRepository = cuotaRepository;
    }

    @Override
    public List<Cuota> obtenerCuotasPorClienteYEstado(String nombre, String estado) {
        return cuotaRepository.findCuotasByClienteDniAndEstado(nombre, estado.toUpperCase());
    }

    @Override
    public List<Prestamo> obtenerPrestamosPorClienteYEstado(String nombre, String estado) {
        return prestamoRepository.findPrestamosByClienteDniAndEstado(nombre, estado.toUpperCase());
    }
}