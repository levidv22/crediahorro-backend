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

    @Scheduled(cron = "0 0 16 * * *") // Todos los días a las 3:35 PM
    public void enviarNotificaciones() {
        List<Cliente> clientes = adminClient.obtenerClientes();
        String adminEmail = authClient.obtenerAdminEmail();
        LocalDate hoy = LocalDate.now();

        for (Cliente cliente : clientes) {
            for (Prestamo prestamo : cliente.getPrestamos()) {
                for (Cuota cuota : prestamo.getCuotas()) {

                    // Saltar cuotas ya pagadas
                    if (cuota.getEstado().equalsIgnoreCase("PAGADA")) {
                        continue;
                    }

                    long diasRestantes = ChronoUnit.DAYS.between(hoy, cuota.getFechaPago());

                    String datosHtml = """
                        <div style="font-family: Arial, sans-serif; color: #333;">
                            <h2 style="color: #007bff;">💼 Detalles del Cliente</h2>
                            <ul>
                                <li><strong>👤 Nombre:</strong> %s</li>
                                <li><strong>🆔 DNI:</strong> %s</li>
                            </ul>
                            <h3 style="color: #007bff;">💳 Detalles del Préstamo</h3>
                            <ul>
                                <li><strong>Monto:</strong> %.2f</li>
                                <li><strong>Estado:</strong> %s</li>
                            </ul>
                            <h3 style="color: #007bff;">📅 Detalles de la Cuota</h3>
                            <ul>
                                <li><strong># Cuota:</strong> %d</li>
                                <li><strong>Monto Cuota:</strong> %.2f</li>
                                <li><strong>Capital:</strong> %.2f</li>
                                <li><strong>Interés:</strong> %.2f</li>
                                <li><strong>Fecha de Pago:</strong> %s</li>
                                <li><strong>Estado Cuota:</strong> %s</li>
                            </ul>
                            <p style="margin-top:20px; font-size: 12px; color: #888; text-align:center;">© 2025 CrediAhorro - Todos los derechos reservados</p>
                        </div>
                        """.formatted(
                            cliente.getNombre(),
                            cliente.getDni(),
                            prestamo.getMonto(),
                            prestamo.getEstado(),
                            cuota.getId(),
                            cuota.getMontoCuota(),
                            cuota.getCapital(),
                            cuota.getInteres(),
                            cuota.getFechaPago(),
                            cuota.getEstado()
                    );

                    String asunto = "";
                    String mensaje = "";

                    if (diasRestantes == 3 || diasRestantes == 2 || diasRestantes == 1) {
                        asunto = "⏰ Aviso: cuota próxima a vencer";
                        mensaje = """
                            <h1 style="color: #ffc107;">⚠️ Cuota próxima a vencer</h1>
                            <p>Estimado administrador,</p>
                            <p>Faltan %d día%s para la fecha de pago de la siguiente cuota:</p>
                            """.formatted(diasRestantes, diasRestantes == 1 ? "" : "s") + datosHtml;

                    } else if (diasRestantes == 0) {
                        asunto = "📅 Aviso: cuota vence hoy";
                        mensaje = """
                            <h1 style="color: #007bff;">📌 Cuota vence hoy</h1>
                            <p>Estimado administrador,</p>
                            <p>Hoy se vence la siguiente cuota:</p>
                            """ + datosHtml;

                    } else if (diasRestantes < 0) {
                        asunto = "❗ Aviso: cuota vencida";
                        mensaje = """
                            <h1 style="color: #dc3545;">🚨 Cuota vencida</h1>
                            <p>Estimado administrador,</p>
                            <p>La siguiente cuota se venció hace %d día%s:</p>
                            """.formatted(Math.abs(diasRestantes), Math.abs(diasRestantes) == 1 ? "" : "s") + datosHtml;
                    }

                    if (!asunto.isEmpty()) {
                        emailService.enviarCorreoHtml(adminEmail, asunto, mensaje);
                    }
                }
            }
        }
    }
}


