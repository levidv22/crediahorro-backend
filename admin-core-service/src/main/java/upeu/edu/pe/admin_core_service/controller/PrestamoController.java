package upeu.edu.pe.admin_core_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.admin_core_service.entities.Prestamo;
import upeu.edu.pe.admin_core_service.service.PrestamoService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @GetMapping(path = "{id}")
    public ResponseEntity<Prestamo> obtenerPrestamoPorId(@PathVariable Long id) {
        Optional<Prestamo> prestamoOpt = prestamoService.obtenerPrestamoPorId(id);
        return prestamoOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(path = "/all")
    public List<Prestamo> getTodosLosPrestamos() {
        return prestamoService.obtenerTodos();
    }

    @PostMapping(path = "/cliente/{clienteId}")
    public ResponseEntity<Prestamo> crearPrestamoParaCliente(@PathVariable Long clienteId,
                                                             @RequestBody Prestamo prestamo) {
        Prestamo creado = prestamoService.crearPrestamoParaCliente(clienteId, prestamo);
        return ResponseEntity.ok(creado);
    }

    @PutMapping(path = "{id}")
    public ResponseEntity<Prestamo> actualizarPrestamo(@PathVariable Long id,
                                                       @RequestBody Prestamo nuevoPrestamo) {
        Prestamo actualizado = prestamoService.actualizarPrestamo(id, nuevoPrestamo);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping(path = "{id}")
    public ResponseEntity<Void> eliminarPrestamo(@PathVariable Long id) {
        prestamoService.eliminarPrestamo(id);
        return ResponseEntity.noContent().build();
    }
}
