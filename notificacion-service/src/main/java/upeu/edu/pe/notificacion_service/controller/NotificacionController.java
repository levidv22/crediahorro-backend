package upeu.edu.pe.notificacion_service.controller;

import org.springframework.http.ResponseEntity;
import upeu.edu.pe.notificacion_service.dto.NotificacionDTO;
import upeu.edu.pe.notificacion_service.service.TwilioService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notificar")
public class NotificacionController {

    private final TwilioService twilioService;

    public NotificacionController(TwilioService twilioService) {
        this.twilioService = twilioService;
    }

    @PostMapping("/recordatorio-cuota")
    public ResponseEntity<Void> recibirNotificacion(@RequestBody NotificacionDTO dto) {


        String mensaje = String.format(
                "🔔 *Recordatorio de Pago de Cuota*\n\n" +
                        "👤 *Cliente:* %s\n" +
                        "📞 *Teléfono:* %s\n\n" +
                        "💳 *Préstamo ID:* %d\n" +
                        "💰 *Monto del préstamo:* S/ %.2f\n\n" +
                        "📅 *Fecha de vencimiento:* %s\n" +
                        "💵 *Monto de la cuota:* S/ %.2f\n\n",
                dto.getNombre(), dto.getTelefono(),
                dto.getPrestamoId(), dto.getMonto_prestamo(),
                dto.getFechaPago(), dto.getMonto_cuota()
        );

                System.out.println("[DEBUG][NOTIFICACION] Recibida notificación de admin-core: " + dto);



        twilioService.enviarMensaje("+51919607831", mensaje);
        return ResponseEntity.ok().build();
    }

}


