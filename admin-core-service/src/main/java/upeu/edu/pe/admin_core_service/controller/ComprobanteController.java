package upeu.edu.pe.admin_core_service.controller;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.*;

@RestController
@RequestMapping(path = "comprobantes")
public class ComprobanteController {

    @Value("${app.upload-dir}")
    private String uploadDir;

    @GetMapping(path = "{nombreArchivo:.+}")
    public ResponseEntity<Resource> obtenerComprobante(@PathVariable String nombreArchivo) throws IOException {
        Path ruta = Paths.get(uploadDir).resolve(nombreArchivo).normalize();

        if (!Files.exists(ruta)) {
            return ResponseEntity.notFound().build();
        }

        UrlResource recurso = new UrlResource(ruta.toUri());
        String contentType = Files.probeContentType(ruta);
        if (contentType == null) contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body((Resource) recurso);
    }
}


