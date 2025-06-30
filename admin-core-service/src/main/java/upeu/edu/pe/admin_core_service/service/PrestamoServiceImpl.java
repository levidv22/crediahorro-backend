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
        double monto = prestamo.getMonto();
        double tasa = prestamo.getTasaInteresMensual() / 100;
        int numeroCuotas = prestamo.getNumeroCuotas();
        LocalDate fechaInicio = prestamo.getFechaInicio();

        double cuota = Math.round((monto * (tasa * Math.pow(1 + tasa, numeroCuotas)) / (Math.pow(1 + tasa, numeroCuotas) - 1)) * 10) * 10.0 / 100.0;

        List<Cuota> cuotas = new ArrayList<>();
        double saldoPendiente = monto;
        for (int i = 0; i < numeroCuotas; i++) {
            double interes = saldoPendiente * tasa;
            double capital = cuota - interes;
            saldoPendiente -= capital;
            Cuota c = new Cuota();
            c.setFechaPago(fechaInicio.plusMonths(i));
            c.setMontoCuota(cuota);
            c.setCapital(Math.round(capital * 100.0) / 100.0);
            c.setInteres(Math.round(interes * 100.0) / 100.0);
            c.setEstado("PENDIENTE");
            cuotas.add(c);
        }

        prestamo.setCuotas(cuotas);
    }
}
