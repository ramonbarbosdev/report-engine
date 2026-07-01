package br.com.reportengine.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record ReportQueryUpsertRequest(
        @NotBlank String nmQuery,
        @NotBlank String dsSql,
        boolean flAtivo
) {
}
