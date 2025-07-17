package upeu.edu.pe.report_grafico.services;

import upeu.edu.pe.report_grafico.dto.ClientePagoDTO;

import java.util.List;
import java.util.Map;

public interface GraficoService {
    Map<String, List<Map<String, Object>>> resumenPorAnioConMeses();
    Map<String, Map<String, Map<String, Double>>> resumenCapitalEInteresPorAdminPorAnio();
    List<ClientePagoDTO> resumenPagosPorAdministrador();
}
