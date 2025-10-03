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

            case "MIXTO":
                aplicarPagoMixto(prestamo, monto);
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

    private void aplicarPagoMixto(Prestamo prestamo, double montoIngresado) {
        double capitalTotal = prestamo.getMonto();
        double interesTotal = prestamo.getInteresTotal();
        int cuotasTotales = prestamo.getNumeroCuotas();
        String tipoCuota = prestamo.getTipoCuota();

        double capitalPagado = prestamo.getCuotas().stream()
                .filter(c -> "PAGADA".equalsIgnoreCase(c.getEstado()))
                .mapToDouble(Cuota::getCapital)
                .sum();

        double interesPagado = prestamo.getCuotas().stream()
                .filter(c -> "PAGADA".equalsIgnoreCase(c.getEstado()))
                .mapToDouble(Cuota::getInteres)
                .sum();

        double capitalPendiente = capitalTotal - capitalPagado;
        double interesPendiente = interesTotal - interesPagado;

        // Validación
        if (montoIngresado < interesPendiente) {
            throw new RuntimeException("Debe pagar al menos todo el interés pendiente antes de reducir el capital.");
        }

        double pagoCapital = montoIngresado - interesPendiente;
        if (pagoCapital > capitalPendiente) {
            throw new RuntimeException("El monto ingresado excede el capital e interés pendiente.");
        }

        double nuevoCapitalPendiente = capitalPendiente - pagoCapital;

        // Eliminar cuotas pendientes actuales
        List<Cuota> cuotasPendientes = prestamo.getCuotas().stream()
                .filter(c -> "PENDIENTE".equalsIgnoreCase(c.getEstado()))
                .toList();

        cuotaRepository.deleteAll(cuotasPendientes);
        prestamo.getCuotas().removeAll(cuotasPendientes);

        // 1. Registrar cuota MIXTA (capital + interés)
        Cuota cuotaMixta = new Cuota();
        cuotaMixta.setFechaPago(LocalDate.now());
        cuotaMixta.setFechaPagada(LocalDate.now());
        cuotaMixta.setCapital(redondearConDecimalFinal0(pagoCapital));
        cuotaMixta.setInteres(redondearConDecimalFinal0(interesPendiente));
        cuotaMixta.setMontoCuota(redondearConDecimalFinal0(montoIngresado));
        cuotaMixta.setEstado("PAGADA");
        cuotaMixta.setTipoPago("Adelanto_MIXTO");
        prestamo.getCuotas().add(cuotaMixta);

        // 2. Reprogramar nuevas cuotas si queda capital
        if (nuevoCapitalPendiente > 0) {
            int cuotasRestantes = cuotasPendientes.size();
            if (cuotasRestantes == 0) {
                cuotasRestantes = 1;
            }

            double interesMensual = prestamo.getTasaInteresMensual();
            List<Cuota> nuevasCuotas = new ArrayList<>();
            LocalDate fechaInicio = LocalDate.now();

            if (tipoCuota.equals("DIARIO")) {
                fechaInicio = fechaInicio.plusDays(1);
            } else {
                fechaInicio = fechaInicio.plusMonths(1);
            }

            double interesNuevo = nuevoCapitalPendiente * (interesMensual / 100);
            double interesTotalActualizado = interesTotal + interesNuevo;
            double montoTotal = capitalTotal + interesNuevo;

            // Actualizar los campos del préstamo
            prestamo.setInteresTotal(redondearConDecimalFinal0(interesTotalActualizado));
            prestamo.setMontoTotal(redondearConDecimalFinal0(montoTotal));

            double montoCuota = redondearConDecimalFinal0((nuevoCapitalPendiente + interesNuevo) / cuotasRestantes);
            double capitalPorCuota = redondearConDecimalFinal0(nuevoCapitalPendiente / cuotasRestantes);
            double interesPorCuota = redondearConDecimalFinal0(interesNuevo / cuotasRestantes);

            for (int i = 0; i < cuotasRestantes; i++) {
                Cuota nueva = new Cuota();
                nueva.setFechaPago(tipoCuota.equals("DIARIO") ? fechaInicio.plusDays(i) : fechaInicio.plusMonths(i));
                nueva.setCapital(capitalPorCuota);
                nueva.setInteres(interesPorCuota);
                nueva.setMontoCuota(capitalPorCuota + interesPorCuota);
                nueva.setEstado("PENDIENTE");
                nueva.setTipoPago("Reprogramado");
                nuevasCuotas.add(nueva);
            }

            prestamo.getCuotas().addAll(nuevasCuotas);
        }
    }

    private double redondearConDecimalFinal0(double valor) {
        double redondeado = Math.round(valor * 10.0) / 10.0;
        return Math.round(redondeado * 10.0) / 10.0;
    }
}

