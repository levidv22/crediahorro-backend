package upeu.edu.pe.report_grafico.repositories;

import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import upeu.edu.pe.report_grafico.beans.LoadBalancerConfiguration;
import upeu.edu.pe.report_grafico.models.Prestamo;

import java.util.List;

@FeignClient(name = "admin-service")
@LoadBalancerClient(name = "admin-service", configuration = LoadBalancerConfiguration.class)
public interface PrestamoRepository {

    @GetMapping("admin-service/prestamos/all")
    List<Prestamo> obtenerTodosLosPrestamos();
}