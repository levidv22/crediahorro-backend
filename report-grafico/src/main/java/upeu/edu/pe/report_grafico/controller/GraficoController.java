package upeu.edu.pe.report_grafico.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import upeu.edu.pe.report_grafico.dto.ClientePagoDTO;
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

    @GetMapping(path = "/capital-interes-por-admin")
    public Map<String, Map<String, Map<String, Double>>> getCapitalEInteresPorAdmin() {
        return graficoService.resumenCapitalEInteresPorAdminPorAnio();
    }

    @GetMapping(path = "/pagos-cliente-por-admin")
    public List<ClientePagoDTO> getPagosClientePorAdmin() {
        return graficoService.resumenPagosPorAdministrador();
    }
}
