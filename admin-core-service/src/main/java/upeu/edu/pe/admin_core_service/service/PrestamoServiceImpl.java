package upeu.edu.pe.admin_core_service.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import upeu.edu.pe.admin_core_service.entities.Cuota;
import upeu.edu.pe.admin_core_service.entities.Prestamo;
import upeu.edu.pe.admin_core_service.repository.ClienteRepository;
import upeu.edu.pe.admin_core_service.repository.PrestamoRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PrestamoServiceImpl implements PrestamoService {


    private final PrestamoRepository prestamoRepository;
    private final ClienteRepository clienteRepository;

    public PrestamoServiceImpl(PrestamoRepository prestamoRepository, ClienteRepository clienteRepository) {
        this.prestamoRepository = prestamoRepository;
        this.clienteRepository = clienteRepository;
    }
    @Override
    public Optional<Prestamo> obtenerPrestamoPorId(Long id) {
        return prestamoRepository.findById(id);
    }

    @Override
    public List<Prestamo> obtenerTodos() {
        return prestamoRepository.findAllPrestamos();
    }

    @Override
    public Prestamo crearPrestamoParaCliente(Long clienteId, Prestamo prestamo) {
        clienteRepository.findById(clienteId).ifPresent(cliente -> {
            List<Prestamo> prestamos = cliente.getPrestamos();
            prestamo.setEstado("ACTIVO");
            prestamo.setFechaCreacion(LocalDate.now());
            generarCuotas(prestamo);
            prestamos.add(prestamo);
            clienteRepository.save(cliente);
        });
        return prestamo;
    }

    @Override
    public Prestamo actualizarPrestamo(Long id, Prestamo nuevo) {
        return prestamoRepository.findById(id).map(prestamo -> {
            prestamo.setFechaInicio(nuevo.getFechaInicio());
            prestamo.setMonto(nuevo.getMonto());
            prestamo.setTasaInteresMensual(nuevo.getTasaInteresMensual());
            prestamo.setNumeroCuotas(nuevo.getNumeroCuotas());

            // Generar nuevas cuotas
            generarCuotas(prestamo);
            return prestamoRepository.save(prestamo);
        }).orElseThrow(() -> new RuntimeException("No se encontró el préstamo"));
    }

    @Override
    public void eliminarPrestamo(Long id) {
        prestamoRepository.deleteById(id);
    }

    private void generarCuotas(Prestamo prestamo) {
        if (prestamo.getTipoCuota().equals("DIARIO")) {
            generarCuotasDiarias(prestamo);
        } else {
            generarCuotasMensuales(prestamo);
        }
    }

    private void generarCuotasMensuales(Prestamo prestamo) {
        double monto = prestamo.getMonto();
        double tasa = prestamo.getTasaInteresMensual() / 100.0;
        int numeroCuotas = prestamo.getNumeroCuotas();
        LocalDate fechaInicio = prestamo.getFechaInicio();

        double cuota = calcularCuota(monto, tasa, numeroCuotas);

        List<Cuota> cuotas = new ArrayList<>();
        double saldoPendiente = monto;

        for (int i = 0; i < numeroCuotas; i++) {
            double interes = saldoPendiente * tasa;
            double capital = cuota - interes;
            saldoPendiente -= capital;

            Cuota nuevaCuota = new Cuota();
            nuevaCuota.setFechaPago(fechaInicio.plusMonths(i));
            nuevaCuota.setMontoCuota(cuota);
            nuevaCuota.setCapital(Math.round(capital * 100.0) / 100.0);
            nuevaCuota.setInteres(Math.round(interes * 100.0) / 100.0);
            nuevaCuota.setEstado("PENDIENTE");

            cuotas.add(nuevaCuota);
        }
        prestamo.setCuotas(cuotas);
    }

    private void generarCuotasDiarias(Prestamo prestamo) {
        double monto = prestamo.getMonto();
        double tasaAnual = prestamo.getTasaInteresMensual() * 12 / 100.0; // Convierte tasa mensual a anual
        double tasaDiaria = tasaAnual / 360; // O usa 365 según tu política
        LocalDate fechaInicio = prestamo.getFechaInicio();

        int numeroDias = prestamo.getNumeroCuotas(); // Aquí número de cuotas es número de días

        double pagoDiario = monto / numeroDias; // capital diario base
        double saldoPendiente = monto;

        List<Cuota> cuotas = new ArrayList<>();

        for (int i = 0; i < numeroDias; i++) {
            double interes = saldoPendiente * tasaDiaria;
            double capital = pagoDiario;
            double montoCuota = capital + interes;
            saldoPendiente -= capital;

            Cuota nuevaCuota = new Cuota();
            nuevaCuota.setFechaPago(fechaInicio.plusDays(i));
            nuevaCuota.setMontoCuota(Math.round(montoCuota * 100.0) / 100.0);
            nuevaCuota.setCapital(Math.round(capital * 100.0) / 100.0);
            nuevaCuota.setInteres(Math.round(interes * 100.0) / 100.0);
            nuevaCuota.setEstado("PENDIENTE");

            cuotas.add(nuevaCuota);
        }
        prestamo.setCuotas(cuotas);
    }

    private double calcularCuota(double monto, double tasa, int n) {
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
}
