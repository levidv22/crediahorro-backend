package upeu.edu.pe.admin_core_service.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.admin_core_service.dto.ClientePagoDTO;
import upeu.edu.pe.admin_core_service.entities.Cliente;
import upeu.edu.pe.admin_core_service.entities.Prestamo;
import upeu.edu.pe.admin_core_service.helpers.JwtHelperAdmin;
import upeu.edu.pe.admin_core_service.service.ClienteService;
import upeu.edu.pe.admin_core_service.service.ExportExcelService;
import upeu.edu.pe.admin_core_service.service.PrestamoService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;
    private final JwtHelperAdmin jwtHelper;
    private final ClienteService clienteService;
    private final ExportExcelService exportExcelService;

    public PrestamoController(PrestamoService prestamoService, JwtHelperAdmin jwtHelper, ClienteService clienteService, ExportExcelService exportExcelService) {
        this.prestamoService = prestamoService;
        this.jwtHelper = jwtHelper;
        this.clienteService = clienteService;
        this.exportExcelService = exportExcelService;
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

    @GetMapping(path = "/exportar/prestamo-pagado/{prestamoId}")
    public ResponseEntity<InputStreamResource> exportarPrestamoPagado(@PathVariable Long prestamoId) throws IOException {
        Optional<Cliente> clienteOpt = clienteService.obtenerTodosLosClientes()
                .stream()
                .filter(c -> c.getPrestamos().stream().anyMatch(p -> p.getId().equals(prestamoId)))
                .findFirst();

        if (clienteOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Cliente cliente = clienteOpt.get();

        Optional<Prestamo> prestamoOpt = cliente.getPrestamos()
                .stream()
                .filter(p -> p.getId().equals(prestamoId) && "PAGADO".equalsIgnoreCase(p.getEstado()))
                .findFirst();

        if (prestamoOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        ByteArrayInputStream stream = exportExcelService.exportarPrestamoPagado(cliente, prestamoOpt.get());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=reporte-prestamo-" + prestamoId + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(stream));
    }
}
