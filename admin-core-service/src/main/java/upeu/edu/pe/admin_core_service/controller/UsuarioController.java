package upeu.edu.pe.admin_core_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.admin_core_service.entities.Cliente;
import upeu.edu.pe.admin_core_service.entities.Cuota;
import upeu.edu.pe.admin_core_service.entities.Prestamo;
import upeu.edu.pe.admin_core_service.helpers.JwtHelperAdmin;
import upeu.edu.pe.admin_core_service.service.ClienteService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "usuarios")
public class UsuarioController {

    private final ClienteService clienteService;
    private final JwtHelperAdmin jwtHelper;

    public UsuarioController(ClienteService clienteService, JwtHelperAdmin jwtHelper) {
        this.clienteService = clienteService;
        this.jwtHelper = jwtHelper;
    }

    @GetMapping(path = "/mis-prestamos")
    public ResponseEntity<List<Prestamo>> obtenerPrestamosDelCliente(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = authorizationHeader.replace("Bearer ", "");
        String username = jwtHelper.extractUsername(token);
        String role = jwtHelper.extractRole(token);

        if (!"USUARIO".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Solo los clientes acceden
        }

        Optional<Cliente> clienteOpt = clienteService.obtenerTodosLosClientes()
                .stream()
                .filter(c -> c.getDni().equals(username)) // username = dni
                .findFirst();

        if (clienteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(clienteOpt.get().getPrestamos());
    }

    @GetMapping(path = "/mis-cuotas/{prestamoId}")
    public ResponseEntity<List<Cuota>> obtenerCuotasDelPrestamo(
            @PathVariable Long prestamoId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = authorizationHeader.replace("Bearer ", "");
        String username = jwtHelper.extractUsername(token);
        String role = jwtHelper.extractRole(token);

        if (!"USUARIO".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Cliente> clienteOpt = clienteService.obtenerTodosLosClientes()
                .stream()
                .filter(c -> c.getDni().equals(username)) // Asegura que sea el suyo
                .findFirst();

        if (clienteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return clienteOpt.get().getPrestamos().stream()
                .filter(p -> p.getId().equals(prestamoId))
                .findFirst()
                .map(p -> ResponseEntity.ok(p.getCuotas()))
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }
}
