package br.com.reportengine.api.dto;

import java.util.List;

public record ReportDefinitionDTO(
        String cdRelatorio,
        String nmRelatorio,
        String nmCategoria,
        String dsRelatorio,
        List<String> dsFormatosSaida,
        List<ReportFilterDTO> filtros
) {
}
