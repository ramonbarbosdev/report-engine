package br.com.reportengine.core;

import br.com.reportengine.domain.entity.ReportDefinitionEntity;
import br.com.reportengine.domain.entity.ReportFilterEntity;
import br.com.reportengine.domain.enums.FilterType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Monta parametros Jasper de forma dinamica a partir dos filtros cadastrados no relatorio.
 * Templates usam $P{PERIODO_RELATORIO} e $P{FILTROS_APLICADOS} sem logica por relatorio.
 */
@Component
public class ReportJasperParameterBuilder {

    private static final DateTimeFormatter BR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final List<String[]> KNOWN_DATE_PAIRS = List.of(
            new String[]{"dataInicial", "dataFinal"},
            new String[]{"dataInicialProjeto", "dataFinalProjeto"},
            new String[]{"dtSegundaParcelaInicio", "dtSegundaParcelaFinal"}
    );

    public Map<String, Object> build(
            ReportDefinitionEntity relatorio,
            Map<String, Object> filtros,
            String nmSolicitante
    ) {
        Map<String, Object> params = new LinkedHashMap<>();
        List<ReportFilterEntity> filterDefs = relatorio.getFiltros().stream()
                .sorted(Comparator.comparing(ReportFilterEntity::getNuOrdem))
                .toList();

        filtros.forEach((key, value) -> {
            if (value != null) {
                params.put(key, toJasperValue(value));
            }
        });

        DateRange period = resolveDateRange(filterDefs, filtros);
        params.put("PERIODO_RELATORIO", formatPeriod(period));
        params.put("FILTROS_APLICADOS", formatAppliedFilters(filterDefs, filtros, period.keys()));

        if (nmSolicitante != null && !nmSolicitante.isBlank()) {
            params.put("USUARIO_EMISSAO", nmSolicitante);
        }

        return params;
    }

    private String formatPeriod(DateRange period) {
        if (period.start() == null && period.end() == null) {
            return "Todo o período";
        }
        if (period.start() != null && period.end() != null) {
            return period.start().format(BR_DATE) + " a " + period.end().format(BR_DATE);
        }
        if (period.start() != null) {
            return "A partir de " + period.start().format(BR_DATE);
        }
        return "Até " + period.end().format(BR_DATE);
    }

    private String formatAppliedFilters(
            List<ReportFilterEntity> filterDefs,
            Map<String, Object> filtros,
            Set<String> periodKeys
    ) {
        List<String> parts = new ArrayList<>();
        for (ReportFilterEntity filter : filterDefs) {
            String key = filter.getNmChave();
            if (periodKeys.contains(key) || filter.getTpFiltro() == FilterType.DATE) {
                continue;
            }
            Object value = filtros.get(key);
            if (value == null) {
                continue;
            }
            parts.add(filter.getDsRotulo() + ": " + formatFilterValue(value));
        }

        if (parts.isEmpty()) {
            return "Nenhum filtro adicional";
        }
        return String.join(" | ", parts);
    }

    private String formatFilterValue(Object value) {
        if (value instanceof LocalDate date) {
            return date.format(BR_DATE);
        }
        return String.valueOf(value);
    }

    private DateRange resolveDateRange(List<ReportFilterEntity> filterDefs, Map<String, Object> filtros) {
        for (String[] pair : KNOWN_DATE_PAIRS) {
            LocalDate start = asDate(filtros.get(pair[0]));
            LocalDate end = asDate(filtros.get(pair[1]));
            if (start != null || end != null) {
                return new DateRange(Set.of(pair[0], pair[1]), start, end);
            }
        }

        Set<String> dateKeys = new LinkedHashSet<>();
        for (ReportFilterEntity filter : filterDefs) {
            if (filter.getTpFiltro() != FilterType.DATE) {
                continue;
            }
            String key = filter.getNmChave();
            if (!key.endsWith("Inicial") && !key.endsWith("inicial")) {
                continue;
            }
            String prefix = key.substring(0, key.length() - "Inicial".length());
            String endKey = prefix + "Final";
            LocalDate start = asDate(filtros.get(key));
            LocalDate end = asDate(filtros.get(endKey));
            if (start != null || end != null) {
                dateKeys.add(key);
                dateKeys.add(endKey);
                return new DateRange(dateKeys, start, end);
            }
        }

        return DateRange.empty();
    }

    private LocalDate asDate(Object value) {
        if (value instanceof LocalDate date) {
            return date;
        }
        if (value instanceof String text && !text.isBlank()) {
            return LocalDate.parse(text.substring(0, Math.min(10, text.length())));
        }
        return null;
    }

    private Object toJasperValue(Object value) {
        if (value instanceof LocalDate date) {
            return date.toString();
        }
        if (value instanceof Number number) {
            if (number instanceof Double || number instanceof Float) {
                double d = number.doubleValue();
                if (d == Math.rint(d) && d >= Long.MIN_VALUE && d <= Long.MAX_VALUE) {
                    return (long) d;
                }
                return d;
            }
            return number.longValue();
        }
        return value;
    }

    private record DateRange(Set<String> keys, LocalDate start, LocalDate end) {
        static DateRange empty() {
            return new DateRange(Set.of(), null, null);
        }
    }
}
