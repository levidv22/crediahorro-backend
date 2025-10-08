package upeu.edu.pe.admin_core_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.admin_core_service.entities.Cliente;
import upeu.edu.pe.admin_core_service.entities.Cuota;
import upeu.edu.pe.admin_core_service.entities.SolicitudPago;
import upeu.edu.pe.admin_core_service.repository.*;
import upeu.edu.pe.admin_core_service.service.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "admin-solicitudes-pago")
public class SolicitudPagoAdminController {

    private final SolicitudPagoRepository solicitudPagoRepository;
    private final CuotaRepository cuotaRepository;
    private final ClienteRepository clienteRepository;
    private final CuotaService cuotaService;

    public SolicitudPagoAdminController(SolicitudPagoRepository repo, CuotaRepository cuotaRepository, CuotaService cuotaService, ClienteRepository clienteRepository) {
        this.solicitudPagoRepository = repo;
        this.cuotaRepository = cuotaRepository;
        this.cuotaService = cuotaService;
        this.clienteRepository = clienteRepository;
    }

    // 🔹 Ver solicitudes pendientes
    @GetMapping(path = "/pendientes")
    public List<Map<String, Object>> listarPendientes() {
        List<SolicitudPago> solicitudes = solicitudPagoRepository.findByEstado("PENDIENTE");

        return solicitudes.stream().map(s -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", s.getId());
            dto.put("tipoSolicitud", s.getTipoSolicitud());
            dto.put("mensajeCliente", s.getMensajeCliente());
            dto.put("estado", s.getEstado());
            dto.put("fechaSolicitud", s.getFechaSolicitud());
            dto.put("montoParcial", s.getMontoParcial());

            if (s.getComprobante() != null) {
                String comprobanteUrl = "https://gateway-production-e6b2.up.railway.app/admin-service/comprobantes/" + s.getId();
                dto.put("comprobanteUrl", comprobanteUrl);
            } else {
                dto.put("comprobanteUrl", null);
            }

            // 🔹 Agregar nombre del cliente
            Cliente cliente = clienteRepository.findById(s.getClienteId()).orElse(null);
            dto.put("clienteNombre", cliente != null ? cliente.getNombre() : "Desconocido");

            // 🔹 Agregar datos de la cuota
            Cuota cuota = cuotaRepository.findById(s.getCuotaId()).orElse(null);
            if (cuota != null) {
                dto.put("cuotaId", cuota.getId());
                dto.put("montoCuota", cuota.getMontoCuota());
                dto.put("fechaPago", cuota.getFechaPago());
                dto.put("estadoCuota", cuota.getEstado());
                dto.put("tipoPago", cuota.getTipoPago());
            }

            return dto;
        }).collect(Collectors.toList());
    }

    // 🔹 Aceptar o rechazar una solicitud
    @PostMapping(path = "{id}/responder")
    public ResponseEntity<?> responderSolicitud(
            @PathVariable Long id,
            @RequestParam boolean aceptar,
            @RequestParam(required = false) String mensajeAdmin,
            @RequestParam(required = false) Double montoParcial // opcional si el admin ajusta monto
    ) {
        Optional<SolicitudPago> solicitudOpt = solicitudPagoRepository.findById(id);

        if (solicitudOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Solicitud no encontrada");
        }

        SolicitudPago solicitud = solicitudOpt.get();

        solicitud.setEstado(aceptar ? "ACEPTADO" : "RECHAZADO");
        solicitud.setMensajeAdministrador(mensajeAdmin);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        solicitudPagoRepository.save(solicitud);

        Cuota cuota = cuotaRepository.findById(solicitud.getCuotaId())
                .orElseThrow(() -> new RuntimeException("Cuota no encontrada"));

        if (aceptar) {
            // 🔹 Determinar acción según tipoSolicitud del cliente
            switch (solicitud.getTipoSolicitud().toUpperCase()) {
                case "PAGO_COMPLETO":
                    cuotaService.pagarCuota(solicitud.getCuotaId());
                    break;

                case "PAGO_PARCIAL":
                    if (solicitud.getMontoParcial() == null || solicitud.getMontoParcial() <= 0) {
                        return ResponseEntity.badRequest().body("El cliente no indicó el monto parcial");
                    }
                    cuotaService.pagarCuotaParcial(solicitud.getCuotaId(), solicitud.getMontoParcial());
                    break;

                case "NO_PAGAR":
                    cuotaService.marcarCuotaComoNoPagadaYReprogramar(solicitud.getCuotaId());
                    break;

                default:
                    return ResponseEntity.badRequest().body("Tipo de solicitud no válido");
            }
        } else {
            cuota.setEstado("PENDIENTE");
            cuotaRepository.save(cuota);
        }

        return ResponseEntity.ok("Solicitud procesada correctamente (" + solicitud.getTipoSolicitud() + ").");
    }
}


