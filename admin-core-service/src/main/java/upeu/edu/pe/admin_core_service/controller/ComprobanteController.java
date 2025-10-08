package upeu.edu.pe.admin_core_service.controller;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.admin_core_service.entities.SolicitudPago;
import upeu.edu.pe.admin_core_service.repository.SolicitudPagoRepository;

import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;

@RestController
@RequestMapping(path = "comprobantes")
public class ComprobanteController {

    private final SolicitudPagoRepository solicitudPagoRepository;

    public ComprobanteController(SolicitudPagoRepository solicitudPagoRepository) {
        this.solicitudPagoRepository = solicitudPagoRepository;
    }

    @GetMapping(path = "{id}")
    public ResponseEntity<byte[]> obtenerComprobante(@PathVariable Long id) {
        Optional<SolicitudPago> solicitudOpt = solicitudPagoRepository.findById(id);

        if (solicitudOpt.isEmpty() || solicitudOpt.get().getComprobante() == null) {
            return ResponseEntity.notFound().build();
        }

        SolicitudPago solicitud = solicitudOpt.get();

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // o IMAGE_PNG, según tipo esperado
                .body(solicitud.getComprobante());
    }
}


