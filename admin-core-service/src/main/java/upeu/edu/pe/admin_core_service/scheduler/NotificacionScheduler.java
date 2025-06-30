package upeu.edu.pe.admin_core_service.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import upeu.edu.pe.admin_core_service.configs.NotificacionClient;
import upeu.edu.pe.admin_core_service.dto.NotificacionDTO;
import upeu.edu.pe.admin_core_service.entities.Cliente;
import upeu.edu.pe.admin_core_service.entities.Cuota;
import upeu.edu.pe.admin_core_service.entities.Prestamo;
import upeu.edu.pe.admin_core_service.repository.ClienteRepository;
import upeu.edu.pe.admin_core_service.repository.CuotaRepository;
import upeu.edu.pe.admin_core_service.repository.PrestamoRepository;
import upeu.edu.pe.admin_core_service.service.ClienteService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@Component
public class NotificacionScheduler {

    private final NotificacionClient notificacionClient; // Feign o RestTemplate
    @Autowired
    private CuotaRepository cuotaRepository;
    @Autowired
    private PrestamoRepository prestamoRepository;
    @Autowired
    private ClienteRepository clienteRepository;

    public NotificacionScheduler(PrestamoRepository prestamoRepository, NotificacionClient notificacionClient, ClienteRepository clienteRepository, CuotaRepository cuotaRepository) {
        this.prestamoRepository = prestamoRepository;
        this.notificacionClient = notificacionClient;
        this.clienteRepository = clienteRepository;
        this.cuotaRepository = cuotaRepository;
    }


   //  @Scheduled(cron = "0 * * * * *") para pruebas
   @Scheduled(cron = "0 0 8 * * *")
   public void revisarCuotas() {
        System.out.println("[DEBUG][ADMIN-CORE] Ejecutando revisarCuotas() " + LocalDateTime.now());
        LocalDate manana = LocalDate.now().plusDays(1);

        // Obtén cuotas pendientes para mañana (asumiendo que tienes este método)
        List<Cuota> cuotas = cuotaRepository.findByEstadoAndFechaPago("PENDIENTE", manana);

        for (Cuota cuota : cuotas) {
            // 1. Busca el id del préstamo usando el id de cuota
            Long idPrestamo = cuotaRepository.findPrestamoIdByCuotaId(cuota.getId());

            // 2. Busca el préstamo
            Prestamo prestamo = prestamoRepository.findById(idPrestamo).orElse(null);
            if (prestamo == null) continue;

            // 3. Busca el id del cliente usando el id del préstamo (usa tu propio método o el campo id_clientes)
            Long idCliente = prestamoRepository.findClienteIdByPrestamoId(prestamo.getId());

            // 4. Busca el cliente
            Cliente cliente = clienteRepository.findById(idCliente).orElse(null);
            if (cliente == null) continue;

            NotificacionDTO dto = new NotificacionDTO();
            dto.setNombre(cliente.getNombre());
            dto.setTelefono(cliente.getTelefonoWhatsapp());
            dto.setPrestamoId(prestamo.getId());
            dto.setMonto_prestamo(prestamo.getMonto());
            dto.setFechaPago(cuota.getFechaPago());
            dto.setMonto_cuota(cuota.getMontoCuota());

            System.out.println("[DEBUG][ADMIN-CORE] Enviando notificación para: " + dto.getNombre() + " " + dto.getTelefono());
            notificacionClient.enviarNotificacion(dto);
        }
    }
}