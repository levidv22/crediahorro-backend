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
import java.time.format.DateTimeFormatter;
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

    @Scheduled(cron = "0 0 14 * * *") // Todos los días a las 6:00 AM
    public void enviarNotificaciones() {
        List<Cliente> clientes = adminClient.obtenerClientes();
        String adminEmail = authClient.obtenerAdminEmail();
        LocalDate hoy = LocalDate.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Cliente cliente : clientes) {
            for (Prestamo prestamo : cliente.getPrestamos()) {
                for (Cuota cuota : prestamo.getCuotas()) {

                    // Saltar cuotas pagadas
                    if (cuota.getEstado().equalsIgnoreCase("PAGADA")) {
                        continue;
                    }

                    long diasRestantes = ChronoUnit.DAYS.between(hoy, cuota.getFechaPago());

                    // Título dinámico del correo
                    String titulo = "";
                    String subtitulo = "";
                    String color = "";

                    if (diasRestantes == 3 || diasRestantes == 2 || diasRestantes == 1) {
                        titulo = "⏰ Cuota próxima a vencer";
                        subtitulo = "Faltan %d día%s para la fecha de pago de la siguiente cuota.".formatted(
                                diasRestantes, diasRestantes == 1 ? "" : "s");
                        color = "#ffc107"; // amarillo

                    } else if (diasRestantes == 0) {
                        titulo = "📅 Cuota vence hoy";
                        subtitulo = "Hoy se vence la siguiente cuota.";
                        color = "#007bff"; // azul

                    } else if (diasRestantes < 0) {
                        titulo = "❗ Cuota vencida";
                        subtitulo = "La siguiente cuota se venció hace %d día%s.".formatted(
                                Math.abs(diasRestantes), Math.abs(diasRestantes) == 1 ? "" : "s");
                        color = "#dc3545"; // rojo
                    }

                    if (!titulo.isEmpty()) {
                        String htmlContent = """
                            <div style="background: #f9f9f9; padding: 40px 0;">
                              <div style="max-width: 600px; margin: auto; background: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); overflow: hidden; font-family: Arial, sans-serif;">
                                <div style="background: %s; color: #fff; padding: 20px; text-align: center;">
                                  <h1 style="margin: 0; font-size: 24px;">%s</h1>
                                </div>
                                <div style="padding: 30px; color: #333;">
                                  <p style="font-size: 16px;">Hola <strong>Administrador</strong>,</p>
                                  <p style="font-size: 16px;">%s</p>
                                  <hr style="border: none; border-top: 1px solid #ddd; margin: 20px 0;">
                                  <h3 style="color: #007bff;">📋 Información del Cliente</h3>
                                  <table style="width: 100%%; border-collapse: collapse;">
                                    <tr><td><strong>👤 Nombre:</strong></td><td>%s</td></tr>
                                    <tr><td><strong>🆔 DNI:</strong></td><td>%s</td></tr>
                                  </table>
                                  <h3 style="color: #007bff; margin-top: 20px;">💳 Información del Préstamo</h3>
                                  <table style="width: 100%%; border-collapse: collapse;">
                                    <tr><td><strong>Monto:</strong></td><td>%.2f</td></tr>
                                    <tr><td><strong>Estado:</strong></td><td>%s</td></tr>
                                  </table>
                                  <h3 style="color: #007bff; margin-top: 20px;">📅 Información de la Cuota</h3>
                                  <table style="width: 100%%; border-collapse: collapse;">
                                    <tr><td><strong>Monto Cuota:</strong></td><td>%.2f</td></tr>
                                    <tr><td><strong>Fecha de Pago:</strong></td><td>%s</td></tr>
                                    <tr><td><strong>Estado Cuota:</strong></td><td>%s</td></tr>
                                  </table>
                                  <p style="text-align: center; font-size: 12px; color: #aaa;">© 2025 CrediAhorro - Todos los derechos reservados</p>
                                </div>
                              </div>
                            </div>
                            """.formatted(
                                color, // banner color
                                titulo,
                                subtitulo,
                                cliente.getNombre(),
                                cliente.getDni(),
                                prestamo.getMonto(),
                                prestamo.getEstado(),
                                cuota.getMontoCuota(),
                                cuota.getFechaPago().format(formato),
                                cuota.getEstado()
                        );

                        emailService.enviarCorreoHtml(adminEmail, titulo, htmlContent);
                    }
                }
            }
        }
    }
}


