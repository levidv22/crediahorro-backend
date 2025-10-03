package upeu.edu.pe.admin_core_service.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import upeu.edu.pe.admin_core_service.dto.RegisterRequest;
import upeu.edu.pe.admin_core_service.entities.Cliente;
import upeu.edu.pe.admin_core_service.entities.Cuota;
import upeu.edu.pe.admin_core_service.entities.Prestamo;
import upeu.edu.pe.admin_core_service.repository.ClienteRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class ClienteServiceImpl implements ClienteService{

    private ClienteRepository clienteRepository;
    private final RestTemplate restTemplate;

    @Value("${auth-service.url}") // lo pones en application.yml
    private String authServiceUrl;

    public ClienteServiceImpl(ClienteRepository clienteRepository, RestTemplate restTemplate) {
        this.clienteRepository = clienteRepository;
        this.restTemplate = restTemplate;
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
            if (prestamo.getFechaCreacion() == null) {
                prestamo.setFechaCreacion(LocalDate.now());
            }
            generarCuotas(prestamo);
        });

        Cliente clienteGuardado = clienteRepository.save(cliente);
        String randomPassword = generarPasswordAleatorio();

        RegisterRequest req = new RegisterRequest();
        req.setUsername(clienteGuardado.getDni());   // dni será el username
        req.setPassword(randomPassword);      // dni será el password
        req.setWhatsapp(clienteGuardado.getTelefonoWhatsapp());
        req.setEmail(clienteGuardado.getCorreoElectronico());
        req.setRole("USUARIO");

        try {
            restTemplate.postForEntity(authServiceUrl + "/auth-service/auth/register", req, Void.class);
        } catch (Exception e) {
            throw new RuntimeException("Error registrando cliente en auth-service", e);
        }

        clienteGuardado.setUsername(req.getUsername());
        clienteGuardado.setPasswordTemporal(randomPassword);
        clienteGuardado = clienteRepository.save(clienteGuardado);

        return clienteGuardado;
    }

    private String generarPasswordAleatorio() {
        int length = 8;
        String caracteres = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(caracteres.length());
            sb.append(caracteres.charAt(index));
        }
        return sb.toString();
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
