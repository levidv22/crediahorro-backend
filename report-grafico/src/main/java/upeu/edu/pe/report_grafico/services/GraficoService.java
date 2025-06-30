package upeu.edu.pe.report_grafico.services;

import java.util.List;
import java.util.Map;

public interface GraficoService {
    Map<String, List<Map<String, Object>>> resumenPorAnioConMeses();
}
