package upeu.edu.pe.report_ms.controllers;

import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.report_ms.services.ReportService;

@RestController
@RequestMapping(path = "report")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping(path = "/generar")
    public String generarReporte(@RequestParam String nombre,
                                 @RequestParam String tipo,
                                 @RequestParam String estado) {
        String reporte;
        if ("prestamos".equalsIgnoreCase(tipo)) {
            reporte = reportService.generarReportePrestamos(nombre, estado);
        } else if ("cuotas".equalsIgnoreCase(tipo)) {
            reporte = reportService.generarReporteCuotas(nombre, estado);
        } else {
            reporte = "Tipo de reporte no válido.";
        }
        return reporte;
    }
}
