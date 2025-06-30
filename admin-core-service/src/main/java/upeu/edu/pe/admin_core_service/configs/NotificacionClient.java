package upeu.edu.pe.admin_core_service.configs;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import upeu.edu.pe.admin_core_service.dto.NotificacionDTO;

@FeignClient(name = "notificacion-service")
public interface NotificacionClient {
    @PostMapping("/api/notificar/recordatorio-cuota")
    void enviarNotificacion(@RequestBody NotificacionDTO dto);
}


