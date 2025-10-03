package upeu.edu.pe.report_grafico.repositories;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import upeu.edu.pe.report_grafico.dto.ClientePagoDTO;
import upeu.edu.pe.report_grafico.models.Prestamo;

@FeignClient(name = "admin-service", url = "https://admin-service-production-1c43.up.railway.app")
public interface PrestamoRepository {

    @GetMapping(path = "admin-service/prestamos/all")
    List<Prestamo> obtenerTodosLosPrestamos();

    @GetMapping(path = "admin-service/prestamos/pagos-cliente-por-admin")
    List<ClientePagoDTO> obtenerResumenPagosPorAdministrador();
}
