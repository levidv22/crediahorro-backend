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
    public Prestamo aplicarPagoAdelantado(Long prestamoId, double montoAdelantado, String tipoReduccion) {
        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        double tasa = prestamo.getTasaInteresMensual() / 100;
        List<Cuota> cuotasPendientes = prestamo.getCuotas().stream()
                .filter(c -> c.getEstado().equalsIgnoreCase("PENDIENTE"))
                .collect(Collectors.toList());

        if (cuotasPendientes.isEmpty()) {
            throw new RuntimeException("No hay cuotas pendientes para este préstamo.");
        }

        // Calcular el capital restante (no el total de las cuotas, que incluye intereses)
        double capitalRestante = calcularCapitalRestante(prestamo, tasa);

        // Aplicar adelanto directamente al capital
        double nuevoCapital = capitalRestante - montoAdelantado;
        if (nuevoCapital < 0) {
            nuevoCapital = 0;
        }

        int n = cuotasPendientes.size();
        double cuotaOriginal = cuotasPendientes.get(0).getMontoCuota();

        if ("CUOTA".equalsIgnoreCase(tipoReduccion)) {
            // Reducir el valor de la cuota (mantener número de cuotas)
            double nuevaCuota = calcularCuota(nuevoCapital, tasa, n);
            for (Cuota cuota : cuotasPendientes) {
                cuota.setMontoCuota(nuevaCuota);
            }

        } else if ("PLAZO".equalsIgnoreCase(tipoReduccion)) {
            int nuevoNumeroCuotas = calcularNumeroCuotas(nuevoCapital, tasa, cuotaOriginal);

            List<Cuota> nuevasCuotas = new ArrayList<>();

            // Encuentra la última cuota pagada (si existe)
            Optional<Cuota> ultimaCuotaPagada = prestamo.getCuotas().stream()
                    .filter(c -> c.getEstado().equalsIgnoreCase("PAGADA"))
                    .max(Comparator.comparing(Cuota::getFechaPago));

            LocalDate fechaInicio;

            if (ultimaCuotaPagada.isPresent()) {
                // Empieza desde la última fecha pagada + 1 mes
                fechaInicio = ultimaCuotaPagada.get().getFechaPago().plusMonths(1);
            } else {
                // Si no hay cuotas pagadas, usa la primera fecha de las cuotas pendientes
                fechaInicio = cuotasPendientes.get(0).getFechaPago();
            }

            for (int i = 0; i < nuevoNumeroCuotas; i++) {
                Cuota nuevaCuota = new Cuota();
                nuevaCuota.setMontoCuota(cuotaOriginal);
                nuevaCuota.setEstado("PENDIENTE");
                nuevaCuota.setFechaPago(fechaInicio.plusMonths(i));
                nuevasCuotas.add(nuevaCuota);
            }

            prestamo.getCuotas().removeIf(c -> c.getEstado().equalsIgnoreCase("PENDIENTE"));
            prestamo.getCuotas().addAll(nuevasCuotas);
            //prestamo.setNumeroCuotas(nuevoNumeroCuotas);
        } else {
            throw new RuntimeException("Tipo de reducción inválido. Usa 'CUOTA' o 'PLAZO'.");
        }

        // Añadimos el registro del pago adelantado en la tabla 'cuotas'
        Cuota cuotaAdelanto = new Cuota();
        cuotaAdelanto.setFechaPago(LocalDate.now());
        cuotaAdelanto.setMontoCuota(montoAdelantado);
        cuotaAdelanto.setEstado("ADELANTADO");
        cuotaRepository.save(cuotaAdelanto);
        prestamo.getCuotas().add(cuotaAdelanto);

        // Actualizar el número de cuotas con el total de cuotas en la lista
        prestamo.setNumeroCuotas(prestamo.getCuotas().size());

        return prestamoRepository.save(prestamo);
    }

    private double calcularCapitalRestante(Prestamo prestamo, double tasa) {
        List<Cuota> cuotasPendientes = prestamo.getCuotas().stream()
                .filter(c -> c.getEstado().equalsIgnoreCase("PENDIENTE"))
                .collect(Collectors.toList());

        double saldo = 0;
        double cuota = cuotasPendientes.get(0).getMontoCuota();
        int n = cuotasPendientes.size();

        // Fórmula inversa para calcular el capital restante en un préstamo
        saldo = cuota * ((1 - Math.pow(1 + tasa, -n)) / tasa);

        return Math.round(saldo * 100.0) / 100.0;
    }

    private double calcularCuota(double monto, double tasa, int n) {
        //if (tasa == 0) return Math.round((monto / n) * 100.0) / 100.0;
        //double cuota = monto * (tasa * Math.pow(1 + tasa, n)) / (Math.pow(1 + tasa, n) - 1);
        //return Math.round(cuota * 100.0) / 100.0;
        double cuota;
        if (tasa == 0) {
            cuota = monto / n;
        } else {
            cuota = monto * (tasa * Math.pow(1 + tasa, n)) / (Math.pow(1 + tasa, n) - 1);
        }

        // Redondear para que el segundo decimal siempre sea 0
        cuota = Math.round(cuota * 10) * 10.0 / 100.0;

        return cuota;
    }

    private int calcularNumeroCuotas(double monto, double tasa, double cuota) {
        if (tasa == 0) return (int) Math.ceil(monto / cuota);
        int n = (int) Math.ceil(
                Math.log(1 / (1 - (tasa * monto / cuota))) / Math.log(1 + tasa)
        );
        return n;
    }
}
