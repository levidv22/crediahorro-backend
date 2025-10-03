package upeu.edu.pe.admin_core_service.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import upeu.edu.pe.admin_core_service.dto.ClientePagoDTO;
import upeu.edu.pe.admin_core_service.entities.Cliente;
import upeu.edu.pe.admin_core_service.entities.Cuota;
import upeu.edu.pe.admin_core_service.entities.Prestamo;
import upeu.edu.pe.admin_core_service.repository.ClienteRepository;
import upeu.edu.pe.admin_core_service.repository.PrestamoRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
            if (prestamo.getFechaCreacion() == null) {
                prestamo.setFechaCreacion(LocalDate.now());
            }
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
        double tasa = prestamo.getTasaInteresMensual(); // mensual
        int numeroCuotas = prestamo.getNumeroCuotas(); // en meses
        LocalDate fechaInicio = prestamo.getFechaInicio();

        double interesTotal = monto * ((tasa * numeroCuotas) / 100);
        double montoTotal = monto + interesTotal;
        double montoCuota = redondearConDecimalFinal0(montoTotal / numeroCuotas);

        List<Cuota> cuotas = new ArrayList<>();

        for (int i = 0; i < numeroCuotas; i++) {
            Cuota cuota = new Cuota();
            cuota.setFechaPago(fechaInicio.plusMonths(i));
            cuota.setMontoCuota(montoCuota);
            cuota.setCapital(redondearConDecimalFinal0(monto / numeroCuotas));
            cuota.setInteres(redondearConDecimalFinal0(interesTotal / numeroCuotas));
            cuota.setEstado("PENDIENTE");

            cuotas.add(cuota);
        }

        prestamo.setCuotas(cuotas);
        prestamo.setMontoTotal(Math.round(montoTotal * 100.0) / 100.0);
        prestamo.setInteresTotal(Math.round(interesTotal * 100.0) / 100.0);
    }

    private void generarCuotasDiarias(Prestamo prestamo) {
        double monto = prestamo.getMonto();
        double tasa = prestamo.getTasaInteresMensual(); // mensual
        int numeroCuotas = prestamo.getNumeroCuotas(); // en días
        LocalDate fechaInicio = prestamo.getFechaInicio();

        double meses = numeroCuotas / 30.0; // convertir días a meses
        double interesTotal = monto * ((tasa * meses) / 100);
        double montoTotal = monto + interesTotal;
        double montoCuota = redondearConDecimalFinal0(montoTotal / numeroCuotas);

        List<Cuota> cuotas = new ArrayList<>();

        for (int i = 0; i < numeroCuotas; i++) {
            Cuota cuota = new Cuota();
            cuota.setFechaPago(fechaInicio.plusDays(i));
            cuota.setMontoCuota(montoCuota);
            cuota.setCapital(redondearConDecimalFinal0(monto / numeroCuotas));
            cuota.setInteres(redondearConDecimalFinal0(interesTotal / numeroCuotas));
            cuota.setEstado("PENDIENTE");

            cuotas.add(cuota);
        }

        prestamo.setCuotas(cuotas);
        prestamo.setMontoTotal(Math.round(montoTotal * 100.0) / 100.0);
        prestamo.setInteresTotal(Math.round(interesTotal * 100.0) / 100.0);
    }

    @Override
    public double calcularCuota(double monto, double tasa, int numeroCuotas) {
        double interes = monto * (tasa / 100);
        double montoTotal = monto + interes;
        double montoCuota = montoTotal / numeroCuotas;

        // Redondear a dos decimales
        return redondearConDecimalFinal0(montoCuota);
    }

    private double redondearConDecimalFinal0(double valor) {
        double redondeadoArriba = Math.ceil(valor * 10.0) / 10.0; // redondea a 1 decimal
        double resultado = Math.round(redondeadoArriba * 10.0) / 10.0; // fuerza segundo decimal a 0
        return resultado;
    }

    @Override
    public List<ClientePagoDTO> obtenerResumenPagosPorAdministrador() {
        List<Cliente> clientes = clienteRepository.findAll(); // Asegúrate de tener este repositorio

        List<ClientePagoDTO> resultado = new ArrayList<>();

        for (Cliente cliente : clientes) {
            for (Prestamo prestamo : cliente.getPrestamos()) {
                if (prestamo.getCuotas() == null || prestamo.getCuotas().isEmpty()) continue;

                List<Cuota> cuotasPagadas = prestamo.getCuotas().stream()
                        .filter(c -> "PAGADA".equalsIgnoreCase(c.getEstado()))
                        .collect(Collectors.toList());

                if (cuotasPagadas.isEmpty()) continue;

                double totalPagado = cuotasPagadas.stream()
                        .mapToDouble(Cuota::getMontoCuota)
                        .sum();

                LocalDate fechaUltima = cuotasPagadas.stream()
                        .map(Cuota::getFechaPagada)
                        .max(LocalDate::compareTo)
                        .orElse(null);

                resultado.add(new ClientePagoDTO(
                        prestamo.getUsernameAdministrador(),
                        cliente.getNombre(),
                        fechaUltima,
                        totalPagado
                ));
            }
        }

        return resultado;
    }
}
