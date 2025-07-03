package upeu.edu.pe.admin_core_service.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import upeu.edu.pe.admin_core_service.entities.Cliente;
import upeu.edu.pe.admin_core_service.entities.Cuota;
import upeu.edu.pe.admin_core_service.entities.Prestamo;
import upeu.edu.pe.admin_core_service.repository.ClienteRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClienteServiceImpl implements ClienteService{

    private ClienteRepository clienteRepository;

    public ClienteServiceImpl(ClienteRepository clienteRepository) {

        this.clienteRepository = clienteRepository;
    }

    @Override
    public List<Cliente> obtenerTodosLosClientes() {
        return clienteRepository.findAll();
    }

    @Override
    public Cliente guardarCliente(Cliente cliente) {
        cliente.setFechaCreacion(LocalDate.now());
        cliente.getPrestamos().forEach(prestamo -> {
            prestamo.setEstado("ACTIVO");
            prestamo.setFechaCreacion(LocalDate.now());
            generarCuotas(prestamo);
        }); // Guardar cliente con préstamos y cuotas




        return clienteRepository.save(cliente);
    }

    @Override
    public List<Cliente> buscarClientesPorNombre(String nombreParcial) {
        return clienteRepository.findByNombreStartingWith(nombreParcial);
    }

    @Override
    public void generarCuotas(Prestamo prestamo) {
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
            nuevaCuota.setCapital(Math.round(capital * 10) * 10.0 / 100.0);
            nuevaCuota.setInteres(Math.round(interes * 10) * 10.0 / 100.0);
            nuevaCuota.setEstado("PENDIENTE");

            cuotas.add(nuevaCuota);
        }
        prestamo.setCuotas(cuotas);

        double interesTotal = cuotas.stream()
                .mapToDouble(Cuota::getInteres)
                .sum();
        prestamo.setInteresTotal(Math.round(interesTotal * 10) * 10.0 / 100.0);
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
            nuevaCuota.setMontoCuota(Math.round(montoCuota * 10) * 10.0 / 100.0);
            nuevaCuota.setCapital(Math.round(capital * 10) * 10.0 / 100.0);
            nuevaCuota.setInteres(Math.round(interes * 10) * 10.0 / 100.0);
            nuevaCuota.setEstado("PENDIENTE");

            cuotas.add(nuevaCuota);
        }
        prestamo.setCuotas(cuotas);

        double interesTotal = cuotas.stream()
                .mapToDouble(Cuota::getInteres)
                .sum();
        prestamo.setInteresTotal(Math.round(interesTotal * 10) * 10.0 / 100.0);
    }

    @Override
    public double calcularCuota(double monto, double tasa, int n) {
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

    @Override
    public Optional<Cliente> obtenerClientePorId(Long id) {
        return clienteRepository.findById(id);
    }

    @Override
    public Optional<Cliente> actualizarCliente(Long id, Cliente clienteActualizado) {
        return clienteRepository.findById(id).map(cliente -> {
            cliente.setNombre(clienteActualizado.getNombre());
            cliente.setDni(clienteActualizado.getDni());
            cliente.setDireccion(clienteActualizado.getDireccion());
            cliente.setTelefonoWhatsapp(clienteActualizado.getTelefonoWhatsapp());
            cliente.setCorreoElectronico(clienteActualizado.getCorreoElectronico());
            return clienteRepository.save(cliente);
        });
    }

    @Override
    public void eliminarCliente(Long id) {
        clienteRepository.deleteById(id);
    }
}
