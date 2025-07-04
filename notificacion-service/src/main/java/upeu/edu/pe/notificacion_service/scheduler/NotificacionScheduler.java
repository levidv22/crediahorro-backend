package upeu.edu.pe.notificacion_service.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import upeu.edu.pe.notificacion_service.client.AdminClient;
import upeu.edu.pe.notificacion_service.client.AuthClient;
import upeu.edu.pe.notificacion_service.model.Cliente;
import upeu.edu.pe.notificacion_service.model.Cuota;
import upeu.edu.pe.notificacion_service.model.Prestamo;
import upeu.edu.pe.notificacion_service.service.EmailService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NotificacionScheduler {

    private final AdminClient adminClient;
    private final AuthClient authClient;
    private final EmailService emailService;

    public NotificacionScheduler(AdminClient adminClient, AuthClient authClient, EmailService emailService) {
        this.adminClient = adminClient;
        this.authClient = authClient;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 12 15 * * *") // Todos los días a las 8 AM
    public void enviarNotificaciones() {
        List<Cliente> clientes = adminClient.obtenerClientes();
        String adminEmail = authClient.obtenerAdminEmail();
        LocalDate hoy = LocalDate.now();

        for (Cliente cliente : clientes) {
            for (Prestamo prestamo : cliente.getPrestamos()) {
                for (Cuota cuota : prestamo.getCuotas()) {
                    long diasRestantes = ChronoUnit.DAYS.between(hoy, cuota.getFechaPago());

                    String datos = String.format("""
                        Cliente: %s (%s)
                        Préstamo: %.2f | Estado: %s | #Cuota: %d
                        Monto cuota: %.2f | Capital: %.2f | Interés: %.2f
                        Fecha Pago: %s | Estado cuota: %s
                        """,
                            cliente.getNombre(),
                            cliente.getDni(),
                            prestamo.getMonto(),
                            prestamo.getEstado(),
                            cuota.getId(),
                            cuota.getMontoCuota(),
                            cuota.getCapital(),
                            cuota.getInteres(),
                            cuota.getFechaPago(),
                            cuota.getEstado());

                    if (cuota.getEstado().equalsIgnoreCase("PAGADA")) {
                        emailService.enviarCorreo(
                                adminEmail,
                                "✔️ Confirmación de cuota pagada",
                                "✅ La cuota ha sido pagada:\n\n" + datos
                        );
                    } else {
                        if (diasRestantes == 3) {
                            emailService.enviarCorreo(
                                    adminEmail,
                                    "⏰ Aviso: cuota próxima a vencer",
                                    "⚠️ Faltan 3 días para la fecha de pago:\n\n" + datos
                            );
                        } else if (diasRestantes == 0) {
                            emailService.enviarCorreo(
                                    adminEmail,
                                    "📅 Aviso: cuota vence hoy",
                                    "📌 Hoy se vence la cuota:\n\n" + datos
                            );
                        } else if (diasRestantes == -1) {
                            emailService.enviarCorreo(
                                    adminEmail,
                                    "❗ Aviso: cuota vencida ayer",
                                    "🚨 La cuota se venció ayer:\n\n" + datos
                            );
                        }
                    }
                }
            }
        }
    }
}


