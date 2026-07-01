package br.com.reportengine.core;

import br.com.reportengine.domain.entity.ReportDefinitionEntity;
import br.com.reportengine.domain.entity.ReportFilterEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@Service
public class FilterValidationService {

    public Map<String, Object> validateAndNormalize(
            ReportDefinitionEntity relatorio,
            Map<String, Object> rawFiltros
    ) {
        Map<String, Object> input = rawFiltros == null ? Map.of() : rawFiltros;
        Map<String, Object> normalized = new HashMap<>();

        for (ReportFilterEntity filtro : relatorio.getFiltros()) {
            Object value = input.get(filtro.getNmChave());
            if (value == null || (value instanceof String s && s.isBlank())) {
                if (filtro.getDsValorPadrao() != null && !filtro.getDsValorPadrao().isBlank()) {
                    value = filtro.getDsValorPadrao();
                } else if (filtro.isFlObrigatorio()) {
                    throw ReportEngineException.badRequest(
                            "Filtro obrigatorio ausente: " + filtro.getNmChave()
                    );
                } else {
                    normalized.put(filtro.getNmChave(), null);
                    continue;
                }
            }
            normalized.put(filtro.getNmChave(), normalizeValue(filtro, value));
        }

        return normalized;
    }

    private Object normalizeValue(ReportFilterEntity filtro, Object value) {
        return switch (filtro.getTpFiltro()) {
            case DATE -> parseDate(value);
            case NUMBER -> parseNumber(value);
            case BOOLEAN -> parseBoolean(value);
            case TEXT, SELECT, DATETIME -> String.valueOf(value);
        };
    }

    private LocalDate parseDate(Object value) {
        try {
            if (value instanceof LocalDate localDate) {
                return localDate;
            }
            return LocalDate.parse(String.valueOf(value));
        } catch (DateTimeParseException ex) {
            throw ReportEngineException.badRequest("Data invalida: " + value);
        }
    }

    private Number parseNumber(Object value) {
        try {
            if (value instanceof Number number) {
                return number;
            }
            return Double.valueOf(String.valueOf(value));
        } catch (NumberFormatException ex) {
            throw ReportEngineException.badRequest("Numero invalido: " + value);
        }
    }

    private Boolean parseBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }
}
