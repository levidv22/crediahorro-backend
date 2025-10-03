package upeu.edu.pe.report_ms.repositories;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import upeu.edu.pe.report_ms.models.Cuota;
import upeu.edu.pe.report_ms.models.Prestamo;

@FeignClient(name = "admin-service", url = "https://admin-service-production-1c43.up.railway.app")
public interface ConsultaRepository {

    @GetMapping(path = "admin-service/consultas/prestamos")
    List<Prestamo> obtenerPrestamosPorClienteYEstado(@RequestParam("nombre") String nombre,
                                                     @RequestParam("estado") String estado);

    @GetMapping(path = "admin-service/consultas/cuotas")
    List<Cuota> obtenerCuotasPorClienteYEstado(@RequestParam("nombre") String nombre,
                                               @RequestParam("estado") String estado);
}

