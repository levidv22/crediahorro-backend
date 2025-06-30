package upeu.edu.pe.report_ms.services;

public interface ReportService {
    String generarReportePrestamos(String nombre, String estado);
    String generarReporteCuotas(String nombre, String estado);
}

