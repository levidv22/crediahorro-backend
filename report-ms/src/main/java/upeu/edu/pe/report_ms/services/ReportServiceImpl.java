package upeu.edu.pe.report_ms.services;

import org.springframework.stereotype.Service;
import upeu.edu.pe.report_ms.helpers.ReportHelper;
import upeu.edu.pe.report_ms.models.Prestamo;
import upeu.edu.pe.report_ms.models.Cuota;
import upeu.edu.pe.report_ms.repositories.ConsultaRepository;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private final ConsultaRepository consultaRepository;
    private final ReportHelper reportHelper;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ReportServiceImpl(ConsultaRepository consultaRepository, ReportHelper reportHelper) {
        this.consultaRepository = consultaRepository;
        this.reportHelper = reportHelper;
    }

    @Override
    public String generarReportePrestamos(String nombre, String estado) {
        List<Prestamo> prestamos = consultaRepository.obtenerPrestamosPorClienteYEstado(nombre, estado);
        List<Cuota> cuotas = consultaRepository.obtenerCuotasPorClienteYEstado(nombre, "PAGADA");
        cuotas.addAll(consultaRepository.obtenerCuotasPorClienteYEstado(nombre, "PENDIENTE"));
        String historial = generarHistorialCrediticio(nombre, cuotas);
        String reportePrestamos = reportHelper.formatearPrestamos(prestamos);
        return historial + "<hr/>" + reportePrestamos;
    }

    @Override
    public String generarReporteCuotas(String nombre, String estado) {
        List<Cuota> cuotas = consultaRepository.obtenerCuotasPorClienteYEstado(nombre, estado);
        String reporteCuotas = reportHelper.formatearCuotas(cuotas);
        String historial = generarHistorialCrediticio(nombre, cuotas);
        return historial + "<hr/>" + reporteCuotas;
    }

    private String generarHistorialCrediticio(String nombre, List<Cuota> cuotas) {
        List<Cuota> cuotasOrdenadas = cuotas.stream()
                .sorted(Comparator.comparing(Cuota::getFechaPago))
                .collect(Collectors.toList());

        boolean hayAlgunaPagada = cuotasOrdenadas.stream().anyMatch(c -> "PAGADA".equalsIgnoreCase(c.getEstado()));
        if (!hayAlgunaPagada) {
            return "⚠️ Aún no es posible calcular su historial crediticio, ya que todavía no ha pagado su primera cuota.";
        }

        int cuotasAtrasadasCount = 0;
        List<String> detalles = new ArrayList<>();

        for (Cuota c : cuotasOrdenadas) {
            if ("PAGADA".equalsIgnoreCase(c.getEstado()) && c.getFechaPagada() != null) {
                String fechaPagoFmt = c.getFechaPago().format(FORMATTER);
                String fechaPagadaFmt = c.getFechaPagada().format(FORMATTER);

                if (c.getFechaPagada().isAfter(c.getFechaPago())) {
                    cuotasAtrasadasCount++;
                    detalles.add(
                            String.format(
                                    "Cuota ID %d pagada con retraso (%s después de la fecha límite %s).",
                                    c.getId(),
                                    fechaPagadaFmt,
                                    fechaPagoFmt
                            )
                    );
                } else {
                    detalles.add(
                            String.format(
                                    "Cuota ID %d pagada a tiempo (%s).",
                                    c.getId(),
                                    fechaPagadaFmt
                            )
                    );
                }
            }
        }

        if (cuotasAtrasadasCount == 0) {
            return "✅ Buen historial crediticio: todas las cuotas fueron pagadas en tiempo. " +
                    "<br/>Detalles:<br/>" + String.join("<br/>", detalles);
        } else {
            return String.format(
                    "⚠️ Historial crediticio con retrasos: %d cuota%s %s pagada%s después de su fecha límite. " +
                            "<br/>Detalles:<br/>%s",
                    cuotasAtrasadasCount,
                    cuotasAtrasadasCount == 1 ? "" : "s",
                    cuotasAtrasadasCount == 1 ? "fue" : "fueron",
                    cuotasAtrasadasCount == 1 ? "" : "s",
                    String.join("<br/>", detalles)
            );
        }
    }
}
