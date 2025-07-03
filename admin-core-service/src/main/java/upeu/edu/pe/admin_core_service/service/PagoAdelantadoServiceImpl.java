package upeu.edu.pe.admin_core_service.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import upeu.edu.pe.admin_core_service.entities.Cuota;
import upeu.edu.pe.admin_core_service.entities.Prestamo;
import upeu.edu.pe.admin_core_service.repository.CuotaRepository;
import upeu.edu.pe.admin_core_service.repository.PrestamoRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PagoAdelantadoServiceImpl implements PagoAdelantadoService {

    private final PrestamoRepository prestamoRepository;
    private final CuotaRepository cuotaRepository;

    public PagoAdelantadoServiceImpl(PrestamoRepository prestamoRepository, CuotaRepository cuotaRepository) {
        this.prestamoRepository = prestamoRepository;
        this.cuotaRepository = cuotaRepository;
    }

    @Override
    public Prestamo aplicarPagoAdelantado(Long prestamoId, double monto, String tipoPago) {
        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        List<Cuota> cuotasPendientes = prestamo.getCuotas().stream()
                .filter(c -> c.getEstado().equalsIgnoreCase("PENDIENTE"))
                .sorted(Comparator.comparing(Cuota::getFechaPago))
                .collect(Collectors.toList());

        if (cuotasPendientes.isEmpty()) {
            throw new RuntimeException("No hay cuotas pendientes");
        }

        double montoCapital = prestamo.getMonto();
        double interesPagado = prestamo.getCuotas().stream()
                .filter(c -> c.getEstado().equalsIgnoreCase("PAGADA"))
                .mapToDouble(Cuota::getInteres)
                .sum();
        double montoInteres = prestamo.getInteresTotal() - interesPagado;
        if (montoInteres < 0) montoInteres = 0;

        switch (tipoPago.toUpperCase()) {
            case "CAPITAL":
                if (monto != montoCapital) {
                    throw new RuntimeException("El monto debe coincidir exactamente con el saldo de capital");
                }
                aplicarPagoCapital(prestamo, cuotasPendientes, montoCapital, montoInteres);
                break;

            case "INTERES":
                if (monto != montoInteres) {
                    throw new RuntimeException("El monto debe coincidir exactamente con el saldo de interés");
                }
                aplicarPagoInteres(prestamo, cuotasPendientes, montoCapital, montoInteres);
                break;

            case "COMPLETO":
                aplicarPagoCompleto(prestamo);
                break;

            default:
                throw new RuntimeException("Tipo de pago inválido: usa CAPITAL, INTERES o COMPLETO");
        }

        return prestamoRepository.save(prestamo);
    }

    private void aplicarPagoCapital(Prestamo prestamo, List<Cuota> cuotasPendientes, double saldoCapital, double saldoInteres) {
        LocalDate fechaOriginal = cuotasPendientes.get(0).getFechaPago();

        // 1. Elimina todas las pendientes
        List<Cuota> cuotasAPendientesEliminar = prestamo.getCuotas().stream()
                .filter(c -> c.getEstado().equalsIgnoreCase("PENDIENTE"))
                .toList();
        cuotaRepository.deleteAll(cuotasAPendientesEliminar);
        prestamo.getCuotas().removeAll(cuotasAPendientesEliminar);

        // 2. Registra cuota pagada de capital
        Cuota pagadaCapital = new Cuota();
        pagadaCapital.setFechaPago(fechaOriginal);
        pagadaCapital.setFechaPagada(LocalDate.now());
        pagadaCapital.setMontoCuota(saldoCapital);
        pagadaCapital.setCapital(saldoCapital);
        pagadaCapital.setInteres(0);
        pagadaCapital.setEstado("PAGADA");
        pagadaCapital.setTipoPago("Adelanto Capital");
        prestamo.getCuotas().add(pagadaCapital);

        // 3. Verifica si el interés ya está pagado
        boolean interesYaPagado = prestamo.getCuotas().stream()
                .filter(c -> c.getEstado().equalsIgnoreCase("PAGADA"))
                .mapToDouble(Cuota::getInteres).sum() >= prestamo.getInteresTotal();

        if (!interesYaPagado && saldoInteres > 0) {
            // Crear cuota pendiente solo si aún falta
            Cuota pendienteInteres = new Cuota();
            pendienteInteres.setFechaPago(fechaOriginal.plusMonths(1));
            pendienteInteres.setMontoCuota(saldoInteres);
            pendienteInteres.setCapital(0);
            pendienteInteres.setInteres(saldoInteres);
            pendienteInteres.setEstado("PENDIENTE");
            pendienteInteres.setTipoPago("Pendiente Interés");
            prestamo.getCuotas().add(pendienteInteres);
        }

        actualizarEstadoPrestamo(prestamo);
    }

    private void aplicarPagoInteres(Prestamo prestamo, List<Cuota> cuotasPendientes, double saldoCapital, double saldoInteres) {
        LocalDate fechaOriginal = cuotasPendientes.get(0).getFechaPago();

        // 1. Elimina todas las pendientes
        List<Cuota> cuotasAPendientesEliminar = prestamo.getCuotas().stream()
                .filter(c -> c.getEstado().equalsIgnoreCase("PENDIENTE"))
                .toList();
        cuotaRepository.deleteAll(cuotasAPendientesEliminar);
        prestamo.getCuotas().removeAll(cuotasAPendientesEliminar);

        // 2. Registra cuota pagada de interés
        Cuota pagadaInteres = new Cuota();
        pagadaInteres.setFechaPago(fechaOriginal);
        pagadaInteres.setFechaPagada(LocalDate.now());
        pagadaInteres.setMontoCuota(saldoInteres);
        pagadaInteres.setCapital(0);
        pagadaInteres.setInteres(saldoInteres);
        pagadaInteres.setEstado("PAGADA");
        pagadaInteres.setTipoPago("Adelanto Interés");
        prestamo.getCuotas().add(pagadaInteres);

        // 3. Verifica si el capital ya está pagado
        boolean capitalYaPagado = prestamo.getCuotas().stream()
                .filter(c -> c.getEstado().equalsIgnoreCase("PAGADA"))
                .mapToDouble(Cuota::getCapital).sum() >= prestamo.getMonto();

        if (!capitalYaPagado && saldoCapital > 0) {
            Cuota pendienteCapital = new Cuota();
            pendienteCapital.setFechaPago(fechaOriginal.plusMonths(1));
            pendienteCapital.setMontoCuota(saldoCapital);
            pendienteCapital.setCapital(saldoCapital);
            pendienteCapital.setInteres(0);
            pendienteCapital.setEstado("PENDIENTE");
            pendienteCapital.setTipoPago("Pendiente Capital");
            prestamo.getCuotas().add(pendienteCapital);
        }

        actualizarEstadoPrestamo(prestamo);
    }

    private void actualizarEstadoPrestamo(Prestamo prestamo) {
        boolean tienePendientes = prestamo.getCuotas().stream()
                .anyMatch(c -> c.getEstado().equalsIgnoreCase("PENDIENTE"));
        if (!tienePendientes) {
            prestamo.setEstado("PAGADO");
        } else {
            prestamo.setEstado("ACTIVO");
        }
    }

    private void aplicarPagoCompleto(Prestamo prestamo) {
        for (Cuota cuota : prestamo.getCuotas()) {
            cuota.setEstado("PAGADA");
            cuota.setFechaPagada(LocalDate.now());
            cuota.setTipoPago("Completo");
        }
        prestamo.setEstado("PAGADO");
    }
}


