package upeu.edu.pe.admin_core_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import upeu.edu.pe.admin_core_service.entities.SolicitudPago;

import java.util.List;

public interface SolicitudPagoRepository extends JpaRepository<SolicitudPago, Long> {
    List<SolicitudPago> findByEstado(String estado);
    List<SolicitudPago> findByClienteId(Long clienteId);
}
