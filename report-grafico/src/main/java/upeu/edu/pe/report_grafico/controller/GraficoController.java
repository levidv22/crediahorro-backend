package upeu.edu.pe.report_grafico.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import upeu.edu.pe.report_grafico.services.GraficoService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "grafico")
public class GraficoController {

    private final GraficoService graficoService;

    public GraficoController(GraficoService graficoService) {
        this.graficoService = graficoService;
    }

    @GetMapping(path = "/prestamos-por-anio")
    public Map<String, List<Map<String, Object>>> getPrestamosPorAnioConMeses() {
        return graficoService.resumenPorAnioConMeses();
    }
}
