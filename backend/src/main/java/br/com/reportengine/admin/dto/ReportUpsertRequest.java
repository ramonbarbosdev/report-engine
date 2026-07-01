package br.com.reportengine.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReportUpsertRequest(
        @NotBlank String cdRelatorio,
        @NotBlank String nmRelatorio,
        String nmCategoria,
        String dsRelatorio,
        @NotBlank String nmDatasource,
        @NotNull Boolean flAtivo,
        String dsFormatosSaida
) {
}
