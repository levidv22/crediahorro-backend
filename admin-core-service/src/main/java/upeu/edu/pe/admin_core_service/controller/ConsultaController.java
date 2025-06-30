package upeu.edu.pe.admin_core_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import upeu.edu.pe.admin_core_service.entities.Cuota;
import upeu.edu.pe.admin_core_service.entities.Prestamo;
import upeu.edu.pe.admin_core_service.service.ConsultaService;

import java.util.List;

@RestController
@RequestMapping(path = "consultas")
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @GetMapping(path = "/prestamos")
    public ResponseEntity<List<Prestamo>> listarPrestamosPorEstado(
            @RequestParam String nombre,
            @RequestParam String estado) {
        return ResponseEntity.ok(consultaService.obtenerPrestamosPorClienteYEstado(nombre, estado));
    }

    @Operation(summary = "Listar cuotas por cliente y estado")
    @GetMapping(path = "/cuotas")
    public ResponseEntity<List<Cuota>> listarCuotasPorEstado(
            @RequestParam String nombre,
            @RequestParam String estado) {
        return ResponseEntity.ok(consultaService.obtenerCuotasPorClienteYEstado(nombre, estado));
    }
}
