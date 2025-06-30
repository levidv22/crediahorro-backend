package upeu.edu.pe.report_ms.helpers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import upeu.edu.pe.report_ms.models.Prestamo;
import upeu.edu.pe.report_ms.models.Cuota;
import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReportHelper {

    @Value("${report.template.prestamo}")
    private String prestamoTemplate;

    @Value("${report.template.cuota}")
    private String cuotaTemplate;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public String formatearPrestamos(List<Prestamo> prestamos) {
        return prestamos.stream()
                .map(this::reemplazarPlaceholdersPrestamo)
                .collect(Collectors.joining("\n"));
    }

    public String formatearCuotas(List<Cuota> cuotas) {
        return cuotas.stream()
                .map(this::reemplazarPlaceholdersCuota)
                .collect(Collectors.joining("\n"));
    }

    private String reemplazarPlaceholdersPrestamo(Prestamo p) {
        return prestamoTemplate
                .replace("{id}", String.valueOf(p.getId()))
                .replace("{monto}", String.format("%.2f", p.getMonto()))
                .replace("{tasa}", String.format("%.2f%%", p.getTasaInteresMensual()))
                .replace("{cuotas}", String.valueOf(p.getNumeroCuotas()))
                .replace("{fecha_creacion}", p.getFechaCreacion() != null ? p.getFechaCreacion().format(FORMATTER) : "")
                .replace("{estado}", p.getEstado());
    }

    private String reemplazarPlaceholdersCuota(Cuota c) {
        return cuotaTemplate
                .replace("{id}", String.valueOf(c.getId()))
                .replace("{fecha_pago}", c.getFechaPago() != null ? c.getFechaPago().format(FORMATTER) : "")
                .replace("{fecha_pagada}", c.getFechaPagada() != null ? c.getFechaPagada().format(FORMATTER) : "No pagada")
                .replace("{monto}", String.format("%.2f", c.getMontoCuota()))
                .replace("{estado}", c.getEstado());
    }
}
