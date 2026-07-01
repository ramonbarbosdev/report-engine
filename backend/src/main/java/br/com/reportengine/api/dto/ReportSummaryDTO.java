package br.com.reportengine.api.dto;

import java.util.List;

public record ReportSummaryDTO(
        String cdRelatorio,
        String nmRelatorio,
        String nmCategoria,
        boolean flAtivo,
        List<String> dsFormatosSaida
) {
}
