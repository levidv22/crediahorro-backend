package upeu.edu.pe.admin_core_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.admin_core_service.dto.ClientePagoDTO;
import upeu.edu.pe.admin_core_service.entities.Prestamo;
import upeu.edu.pe.admin_core_service.helpers.JwtHelperAdmin;
import upeu.edu.pe.admin_core_service.service.PrestamoService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;
    private final JwtHelperAdmin jwtHelper;

    public PrestamoController(PrestamoService prestamoService, JwtHelperAdmin jwtHelper) {
        this.prestamoService = prestamoService;
        this.jwtHelper = jwtHelper;
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
    public ResponseEntity<Prestamo> crearPrestamoParaCliente(
            @PathVariable Long clienteId,
            @RequestBody Prestamo prestamo,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = authorizationHeader.replace("Bearer ", "");
        String username = jwtHelper.extractUsername(token);

        // Setear el username en el préstamo
        prestamo.setUsernameAdministrador(username);

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

    @GetMapping(path = "/pagos-cliente-por-admin")
    public List<ClientePagoDTO> getResumenPagosPorAdministrador() {
        return prestamoService.obtenerResumenPagosPorAdministrador();
    }

}
