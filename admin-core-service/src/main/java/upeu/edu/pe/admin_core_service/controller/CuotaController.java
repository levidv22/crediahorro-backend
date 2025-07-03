package upeu.edu.pe.admin_core_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.admin_core_service.entities.Cuota;
import upeu.edu.pe.admin_core_service.entities.Prestamo;
import upeu.edu.pe.admin_core_service.repository.PrestamoRepository;
import upeu.edu.pe.admin_core_service.repository.CuotaRepository;
import upeu.edu.pe.admin_core_service.service.CuotaService;
import upeu.edu.pe.admin_core_service.service.PagoAdelantadoService;

import java.util.*;

@RestController
@RequestMapping(path = "cuotas")
public class CuotaController {

    private final PrestamoRepository prestamoRepository;
    private final CuotaService cuotaService;
    private final CuotaRepository cuotaRepository;
    private final PagoAdelantadoService pagoAdelantadoService;

    public CuotaController(PrestamoRepository prestamoRepository, CuotaService cuotaService, CuotaRepository cuotaRepository, PagoAdelantadoService pagoAdelantadoService) {
        this.prestamoRepository = prestamoRepository;
        this.cuotaService = cuotaService;
        this.cuotaRepository = cuotaRepository;
        this.pagoAdelantadoService = pagoAdelantadoService;
    }

    @GetMapping(path = "/prestamo/{prestamoId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> listarCuotasPorPrestamo(@PathVariable Long prestamoId) {
        Optional<Prestamo> prestamoOpt = prestamoRepository.findById(prestamoId);
        if (prestamoOpt.isPresent()) {
            Prestamo prestamo = prestamoOpt.get();
            List<Cuota> cuotas = new ArrayList<>(prestamo.getCuotas());

            // Ordenar cuotas
            cuotas.sort(Comparator.comparing(Cuota::getFechaPago));

            // Calcular pendientes
            long cuotasPendientes = cuotas.stream()
                    .filter(c -> c.getEstado().equalsIgnoreCase("PENDIENTE"))
                    .count();

            Map<String, Object> response = new HashMap<>();
            response.put("cuotas", cuotas);
            response.put("prestamoId", prestamoId);
            response.put("tipoCuota", prestamo.getTipoCuota());
            response.put("cuotasPendientes", cuotasPendientes);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(path = "/{cuotaId}/pagar")
    public ResponseEntity<Void> pagarCuota(@PathVariable Long cuotaId) {
        cuotaService.pagarCuota(cuotaId);
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/prestamos/{prestamoId}/saldos")
    public ResponseEntity<Map<String, Double>> obtenerSaldos(@PathVariable Long prestamoId) {
        Optional<Prestamo> prestamoOpt = prestamoRepository.findById(prestamoId);
        if (prestamoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Prestamo prestamo = prestamoOpt.get();

        double montoCapital = prestamo.getMonto();

        double interesPagado = prestamo.getCuotas().stream()
                .filter(c -> c.getEstado().equalsIgnoreCase("PAGADA"))
                .mapToDouble(Cuota::getInteres)
                .sum();

        double montoInteresPendiente = prestamo.getInteresTotal() - interesPagado;

        if (montoInteresPendiente < 0) montoInteresPendiente = 0;

        double saldoTotal = montoCapital + montoInteresPendiente;

        Map<String, Double> response = new HashMap<>();
        response.put("capital", Math.round(montoCapital * 100.0) / 100.0);
        response.put("interes", Math.round(montoInteresPendiente * 100.0) / 100.0);
        response.put("total", Math.round(saldoTotal * 100.0) / 100.0);

        return ResponseEntity.ok(response);
    }


    @PostMapping(path = "/prestamos/{prestamoId}/pago-adelantado")
    public ResponseEntity<Void> aplicarPagoAdelantado(
            @PathVariable Long prestamoId,
            @RequestParam double monto,
            @RequestParam String tipoPago) {
        pagoAdelantadoService.aplicarPagoAdelantado(prestamoId, monto, tipoPago);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/{cuotaId}/pagar-avanzado")
    public ResponseEntity<Cuota> pagarCuotaAvanzado(@PathVariable Long cuotaId,
                                                    @RequestParam String tipoPago) {
        Cuota cuota = cuotaService.pagarCuotaAvanzado(cuotaId, tipoPago);
        return ResponseEntity.ok(cuota);
    }


}
