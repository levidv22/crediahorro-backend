package upeu.edu.pe.report_grafico.services;

import org.springframework.stereotype.Service;
import upeu.edu.pe.report_grafico.dto.ClientePagoDTO;
import upeu.edu.pe.report_grafico.models.Cuota;
import upeu.edu.pe.report_grafico.models.Prestamo;
import upeu.edu.pe.report_grafico.repositories.PrestamoRepository;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GraficoServiceImpl implements GraficoService {

    private final PrestamoRepository prestamoRepository;

    public GraficoServiceImpl(PrestamoRepository prestamoRepository) {
        this.prestamoRepository = prestamoRepository;
    }

    @Override
    public Map<String, List<Map<String, Object>>> resumenPorAnioConMeses() {
        List<Prestamo> prestamos = prestamoRepository.obtenerTodosLosPrestamos();

        Map<String, List<Map<String, Object>>> resultado = new TreeMap<>();

        Set<Integer> anios = new HashSet<>();
        prestamos.forEach(p -> {
            anios.add(p.getFechaCreacion().getYear());
            p.getCuotas().stream()
                    .filter(c -> "PAGADA".equalsIgnoreCase(c.getEstado()))
                    .forEach(c -> anios.add(c.getFechaPago().getYear()));
        });

        // Inicializa meses para cada año
        for (Integer anio : anios) {
            List<Map<String, Object>> meses = new ArrayList<>();
            for (Month mes : Month.values()) {
                Map<String, Object> mesData = new HashMap<>();
                mesData.put("mes", mes.getDisplayName(TextStyle.FULL, new Locale("es")));
                mesData.put("montoPrestado", 0.0);
                mesData.put("interesPagado", 0.0);
                meses.add(mesData);
            }
            resultado.put(String.valueOf(anio), meses);
        }

        // Monto prestado según fechaCreacion
        for (Prestamo prestamo : prestamos) {
            int anio = prestamo.getFechaCreacion().getYear();
            String mesNombre = prestamo.getFechaCreacion().getMonth().getDisplayName(TextStyle.FULL, new Locale("es"));
            List<Map<String, Object>> meses = resultado.get(String.valueOf(anio));

            Map<String, Object> mesData = meses.stream()
                    .filter(m -> m.get("mes").equals(mesNombre))
                    .findFirst()
                    .orElseThrow();

            mesData.put("montoPrestado", ((Number) mesData.get("montoPrestado")).doubleValue() + prestamo.getMonto());
        }

        // Intereses: por cuota si está ACTIVO, o usar interesTotal si está PAGADO
        for (Prestamo prestamo : prestamos) {
            if ("PAGADO".equalsIgnoreCase(prestamo.getEstado())) {
                // Usar el interesTotal solo cuando el préstamo está PAGADO
                Optional<Cuota> ultimaCuotaPagada = prestamo.getCuotas().stream()
                        .filter(c -> c.getFechaPagada() != null)
                        .max(Comparator.comparing(Cuota::getFechaPagada));

                if (ultimaCuotaPagada.isPresent()) {
                    LocalDate fecha = ultimaCuotaPagada.get().getFechaPagada();
                    String anio = String.valueOf(fecha.getYear());
                    String mesNombre = fecha.getMonth().getDisplayName(TextStyle.FULL, new Locale("es"));
                    List<Map<String, Object>> meses = resultado.get(anio);

                    if (meses != null) {
                        Map<String, Object> mesData = meses.stream()
                                .filter(m -> m.get("mes").equals(mesNombre))
                                .findFirst()
                                .orElseThrow();

                        mesData.put("interesPagado", ((Number) mesData.get("interesPagado")).doubleValue() + prestamo.getInteresTotal());
                    }
                }

            } else {
                // ACTIVO: sumar intereses de cuotas pagadas
                for (Cuota cuota : prestamo.getCuotas()) {
                    if (!"PAGADA".equalsIgnoreCase(cuota.getEstado())) continue;

                    int anio = cuota.getFechaPago().getYear();
                    String mesNombre = cuota.getFechaPago().getMonth().getDisplayName(TextStyle.FULL, new Locale("es"));
                    List<Map<String, Object>> meses = resultado.get(String.valueOf(anio));

                    if (meses != null) {
                        Map<String, Object> mesData = meses.stream()
                                .filter(m -> m.get("mes").equals(mesNombre))
                                .findFirst()
                                .orElseThrow();

                        mesData.put("interesPagado", ((Number) mesData.get("interesPagado")).doubleValue() + cuota.getInteres());
                    }
                }
            }
        }

        // Agrega resumen anual por año
        for (Map.Entry<String, List<Map<String, Object>>> entry : resultado.entrySet()) {
            double totalInteresesAnio = entry.getValue().stream()
                    .mapToDouble(m -> ((Number) m.get("interesPagado")).doubleValue())
                    .sum();

            Map<String, Object> totalData = new HashMap<>();
            totalData.put("mes", "TOTAL");
            totalData.put("interesPagado", totalInteresesAnio);
            entry.getValue().add(totalData);
        }

        return resultado;
    }

    @Override
    public Map<String, Map<String, Map<String, Double>>> resumenCapitalEInteresPorAdminPorAnio() {
        List<Prestamo> prestamos = prestamoRepository.obtenerTodosLosPrestamos();

        Map<String, Map<String, Map<String, Double>>> resumen = new HashMap<>();

        for (Prestamo prestamo : prestamos) {
            String admin = prestamo.getUsernameAdministrador();
            String anio = String.valueOf(prestamo.getFechaCreacion().getYear());
            double capital = prestamo.getMonto();

            // Verificar si todas las cuotas están pagadas
            boolean todasCuotasPagadas = prestamo.getCuotas().stream()
                    .allMatch(c -> "PAGADA".equalsIgnoreCase(c.getEstado()));

            double interes;
            if (todasCuotasPagadas) {
                interes = prestamo.getInteresTotal();  // usar el interés completo
            } else {
                interes = prestamo.getCuotas().stream()
                        .filter(c -> "PAGADA".equalsIgnoreCase(c.getEstado()))
                        .mapToDouble(Cuota::getInteres)
                        .sum();
            }

            resumen
                    .computeIfAbsent(admin, k -> new HashMap<>())
                    .computeIfAbsent(anio, k -> new HashMap<>())
                    .merge("capital", capital, Double::sum);

            resumen
                    .get(admin)
                    .get(anio)
                    .merge("interes", interes, Double::sum);
        }

        return resumen;
    }

    @Override
    public List<ClientePagoDTO> resumenPagosPorAdministrador() {
        return prestamoRepository.obtenerResumenPagosPorAdministrador();
    }
}
