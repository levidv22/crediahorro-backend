package upeu.edu.pe.report_grafico.repositories;

import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import upeu.edu.pe.report_grafico.beans.LoadBalancerConfiguration;
import upeu.edu.pe.report_grafico.dto.ClientePagoDTO;
import upeu.edu.pe.report_grafico.models.Prestamo;

import java.util.List;

@FeignClient(name = "admin-service", url = "https://ms-admin-core.onrender.com")
public interface PrestamoRepository {

    @GetMapping(path = "admin-service/prestamos/all")
    List<Prestamo> obtenerTodosLosPrestamos();

    @GetMapping(path = "admin-service/prestamos/pagos-cliente-por-admin")
    List<ClientePagoDTO> obtenerResumenPagosPorAdministrador();
}
