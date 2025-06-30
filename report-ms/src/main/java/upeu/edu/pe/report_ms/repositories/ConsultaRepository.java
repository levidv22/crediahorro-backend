package upeu.edu.pe.report_ms.repositories;

import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.report_ms.beans.LoadBalancerConfiguration;
import upeu.edu.pe.report_ms.models.Cuota;
import upeu.edu.pe.report_ms.models.Prestamo;

import java.util.List;
import java.util.Optional;

@FeignClient(name = "admin-service")
@LoadBalancerClient(name = "admin-service", configuration = LoadBalancerConfiguration.class)
public interface ConsultaRepository {

    @GetMapping(path = "admin-service/consultas/prestamos")
    List<Prestamo> obtenerPrestamosPorClienteYEstado(@RequestParam("nombre") String nombre,
                                                     @RequestParam("estado") String estado);

    @GetMapping(path = "admin-service/consultas/cuotas")
    List<Cuota> obtenerCuotasPorClienteYEstado(@RequestParam("nombre") String nombre,
                                               @RequestParam("estado") String estado);
}

