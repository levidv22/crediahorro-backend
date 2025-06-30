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
        double monto = prestamo.getMonto();
        double tasa = prestamo.getTasaInteresMensual() / 100; // convertir a decimal
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
