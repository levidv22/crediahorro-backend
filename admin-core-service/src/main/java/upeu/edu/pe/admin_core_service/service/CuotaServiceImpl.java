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
    public Long findPrestamoIdByCuotaId(Long cuotaId) {
        return cuotaRepository.findPrestamoIdByCuotaId(cuotaId);
    }

    @Override
    public Cuota pagarCuotaAvanzado(Long cuotaId, String tipoPago) {
        Cuota cuota = cuotaRepository.findById(cuotaId)
                .orElseThrow(() -> new RuntimeException("Cuota no encontrada"));

        if (cuota.getEstado().equalsIgnoreCase("PAGADA")) {
            throw new IllegalStateException("Esta cuota ya está pagada");
        }

        double capital = cuota.getCapital();
        double interes = cuota.getInteres();
        double nuevoMonto = 0.0;

        if (tipoPago.equalsIgnoreCase("Capital")) {
            pasarAProximaCuota(cuotaId, "interes", interes);
            nuevoMonto = capital;
            cuota.setTipoPago("Capital");

        } else if (tipoPago.equalsIgnoreCase("Interes")) {
            pasarAProximaCuota(cuotaId, "capital", capital);
            nuevoMonto = interes;
            cuota.setTipoPago("Interés");

        } else if (tipoPago.equalsIgnoreCase("Completo")) {
            nuevoMonto = capital + interes;
            cuota.setTipoPago("Completo");

        } else {
            throw new IllegalArgumentException("Tipo de pago inválido");
        }

        cuota.setMontoCuota(nuevoMonto);
        cuota.setEstado("PAGADA");
        cuota.setFechaPagada(LocalDate.now());
        cuotaRepository.save(cuota);

        actualizarEstadoPrestamoSiEsNecesario(cuota);
        return cuota;
    }

    private void pasarAProximaCuota(Long cuotaId, String tipo, double montoTransferir) {
        Cuota actual = cuotaRepository.findById(cuotaId).orElseThrow();
        Long prestamoId = findPrestamoIdByCuotaId(cuotaId);

        Prestamo prestamo = prestamoRepository.findById(prestamoId).orElseThrow();
        List<Cuota> cuotas = prestamo.getCuotas();

        cuotas.sort(Comparator.comparing(Cuota::getFechaPago));
        int index = cuotas.indexOf(actual);
        if (index + 1 < cuotas.size()) {
            Cuota siguiente = cuotas.get(index + 1);
            if (tipo.equals("capital")) {
                siguiente.setCapital(siguiente.getCapital() + montoTransferir);
            } else {
                siguiente.setInteres(siguiente.getInteres() + montoTransferir);
            }
            siguiente.setMontoCuota(siguiente.getCapital() + siguiente.getInteres());
            cuotaRepository.save(siguiente);
        }
    }



}
