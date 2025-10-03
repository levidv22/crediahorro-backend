package upeu.edu.pe.admin_core_service.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import upeu.edu.pe.admin_core_service.entities.Cuota;
import upeu.edu.pe.admin_core_service.entities.Prestamo;
import upeu.edu.pe.admin_core_service.repository.CuotaRepository;
import upeu.edu.pe.admin_core_service.repository.PrestamoRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CuotaServiceImpl implements CuotaService {

    private final CuotaRepository cuotaRepository;
    private final PrestamoRepository prestamoRepository;

    public CuotaServiceImpl(CuotaRepository cuotaRepository, PrestamoRepository prestamoRepository) {
        this.cuotaRepository = cuotaRepository;
        this.prestamoRepository = prestamoRepository;
    }

    @Override
    public Cuota pagarCuota(Long cuotaId) {
        Cuota cuota = cuotaRepository.findById(cuotaId)
                .orElseThrow(() -> new RuntimeException("Cuota no encontrada"));

        cuota.setEstado("PAGADA");
        cuota.setTipoPago("Pagó Completo");
        cuota.setFechaPagada(LocalDate.now());
        cuotaRepository.save(cuota);

        // Revisar si todas las cuotas del préstamo están pagadas
        actualizarEstadoPrestamoSiEsNecesario(cuota);

        return cuota;
    }

    private void actualizarEstadoPrestamoSiEsNecesario(Cuota cuota) {
        // Buscar el préstamo relacionado
        Optional<Prestamo> prestamoOpt = prestamoRepository.findAll().stream()
                .filter(p -> p.getCuotas().stream().anyMatch(c -> c.getId().equals(cuota.getId())))
                .findFirst();

        if (prestamoOpt.isPresent()) {
            Prestamo prestamo = prestamoOpt.get();

            boolean todasPagadasOAdelantadas = prestamo.getCuotas().stream()
                    .allMatch(c -> {
                        String estado = c.getEstado().toUpperCase();
                        return estado.equals("PAGADA") || estado.equals("ADELANTADO");
                    });

            if (todasPagadasOAdelantadas) {
                prestamo.setEstado("PAGADO");
                prestamoRepository.save(prestamo);
            }
        }
    }

    @Override
    public Cuota marcarCuotaComoNoPagadaYReprogramar(Long cuotaId) {
        Cuota cuotaActual = cuotaRepository.findById(cuotaId)
                .orElseThrow(() -> new RuntimeException("Cuota no encontrada"));

        if (!cuotaActual.getEstado().equals("PENDIENTE")) {
            throw new RuntimeException("Solo se puede reprogramar cuotas pendientes");
        }

        // Marcar como no pagada
        cuotaActual.setEstado("PAGADA");
        cuotaActual.setTipoPago("NO_PAGADA");
        cuotaActual.setFechaPagada(LocalDate.now());

        double montoOriginal = cuotaActual.getMontoCuota();
        cuotaActual.setMontoCuota(0);
        cuotaRepository.save(cuotaActual);

        // Buscar siguiente cuota pendiente
        Optional<Prestamo> prestamoOpt = prestamoRepository.findAll().stream()
                .filter(p -> p.getCuotas().stream().anyMatch(c -> c.getId().equals(cuotaId)))
                .findFirst();

        if (prestamoOpt.isPresent()) {
            Prestamo prestamo = prestamoOpt.get();
            List<Cuota> cuotasOrdenadas = prestamo.getCuotas().stream()
                    .sorted(Comparator.comparing(Cuota::getFechaPago))
                    .toList();

            boolean found = false;
            for (Cuota cuota : cuotasOrdenadas) {
                if (found && cuota.getEstado().equals("PENDIENTE")) {
                    // Sumar monto a la siguiente cuota
                    cuota.setMontoCuota(Math.round((cuota.getMontoCuota() + montoOriginal) * 100.0) / 100.0);
                    cuotaRepository.save(cuota);
                    break;
                }
                if (cuota.getId().equals(cuotaActual.getId())) {
                    found = true;
                }
            }
        }

        return cuotaActual;
    }

    @Override
    public Cuota pagarCuotaParcial(Long cuotaId, double montoIngresado) {
        Cuota cuota = cuotaRepository.findById(cuotaId)
                .orElseThrow(() -> new RuntimeException("Cuota no encontrada"));

        if (!cuota.getEstado().equals("PENDIENTE")) {
            throw new RuntimeException("Solo se pueden pagar cuotas pendientes");
        }

        if (montoIngresado > cuota.getMontoCuota()) {
            throw new RuntimeException("El monto ingresado no puede ser mayor a la cuota");
        }

        double diferencia = cuota.getMontoCuota() - montoIngresado;

        // Actualizar cuota actual
        cuota.setMontoPagado(montoIngresado);
        cuota.setMontoCuota(montoIngresado); // actualiza visual
        cuota.setFechaPagada(LocalDate.now());
        cuota.setEstado("PAGADA");
        cuota.setTipoPago("PAGO_INCOMPLETO");
        cuotaRepository.save(cuota);

        // Buscar siguiente cuota pendiente
        Optional<Prestamo> prestamoOpt = prestamoRepository.findAll().stream()
                .filter(p -> p.getCuotas().stream().anyMatch(c -> c.getId().equals(cuotaId)))
                .findFirst();

        if (prestamoOpt.isPresent()) {
            Prestamo prestamo = prestamoOpt.get();
            List<Cuota> cuotasOrdenadas = prestamo.getCuotas().stream()
                    .sorted(Comparator.comparing(Cuota::getFechaPago))
                    .toList();

            boolean found = false;
            for (Cuota siguienteCuota : cuotasOrdenadas) {
                if (found && siguienteCuota.getEstado().equals("PENDIENTE")) {
                    siguienteCuota.setMontoCuota(Math.round((siguienteCuota.getMontoCuota() + diferencia) * 100.0) / 100.0);
                    cuotaRepository.save(siguienteCuota);
                    break;
                }
                if (siguienteCuota.getId().equals(cuotaId)) {
                    found = true;
                }
            }
        }

        return cuota;
    }

    @Override
    public Long findPrestamoIdByCuotaId(Long cuotaId) {
        return cuotaRepository.findPrestamoIdByCuotaId(cuotaId);
    }
}
