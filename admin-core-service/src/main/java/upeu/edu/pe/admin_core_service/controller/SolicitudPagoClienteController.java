package upeu.edu.pe.admin_core_service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import upeu.edu.pe.admin_core_service.entities.*;
import upeu.edu.pe.admin_core_service.helpers.JwtHelperAdmin;
import upeu.edu.pe.admin_core_service.repository.SolicitudPagoRepository;
import upeu.edu.pe.admin_core_service.service.ClienteService;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping(path = "usuarios-solicitudes-pago")
public class SolicitudPagoClienteController {

    private final SolicitudPagoRepository solicitudPagoRepository;
    private final ClienteService clienteService;
    private final JwtHelperAdmin jwtHelper;

    public SolicitudPagoClienteController(SolicitudPagoRepository repo, ClienteService clienteService, JwtHelperAdmin jwtHelper) {
        this.solicitudPagoRepository = repo;
        this.clienteService = clienteService;
        this.jwtHelper = jwtHelper;
    }

    // 🔹 Enviar comprobante de pago
    @PostMapping(path = "{cuotaId}/enviar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> enviarSolicitudPago(
            @PathVariable Long cuotaId,
            @RequestParam String tipoSolicitud, // PAGO_COMPLETO o PAGO_PARCIAL
            @RequestParam(required = false) String mensajeCliente,
            @RequestParam(required = false) MultipartFile comprobante,
            @RequestParam(required = false) Double montoParcial,
            @RequestHeader("Authorization") String authHeader
    ) throws IOException {

        String token = authHeader.replace("Bearer ", "");
        String username = jwtHelper.extractUsername(token);
        String role = jwtHelper.extractRole(token);

        if (!"USUARIO".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
        }

        Optional<Cliente> clienteOpt = clienteService.obtenerTodosLosClientes()
                .stream()
                .filter(c -> c.getDni().equals(username))
                .findFirst();

        if (clienteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente no encontrado");
        }

        Cliente cliente = clienteOpt.get();

        // 🔹 Crear solicitud
        SolicitudPago solicitud = new SolicitudPago();
        solicitud.setCuotaId(cuotaId);
        solicitud.setClienteId(cliente.getId());
        solicitud.setTipoSolicitud(tipoSolicitud);
        solicitud.setMensajeCliente(mensajeCliente);
        solicitud.setMontoParcial(montoParcial);
        solicitud.setEstado("PENDIENTE");
        solicitud.setFechaSolicitud(LocalDateTime.now());

        if (comprobante != null && !comprobante.isEmpty()) {
            solicitud.setComprobante(comprobante.getBytes());
        }

        solicitudPagoRepository.save(solicitud);

        return ResponseEntity.ok("Pago enviado correctamente. Estado: PENDIENTE de aprobación.");
    }

    // 🔹 Listar todas las solicitudes del cliente
    @GetMapping(path = "/mis-solicitudes")
    public ResponseEntity<List<SolicitudPago>> listarSolicitudesCliente(
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.replace("Bearer ", "");
        String username = jwtHelper.extractUsername(token);

        Optional<Cliente> clienteOpt = clienteService.obtenerTodosLosClientes()
                .stream()
                .filter(c -> c.getDni().equals(username))
                .findFirst();

        if (clienteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(
                solicitudPagoRepository.findByClienteId(clienteOpt.get().getId())
        );
    }
}

