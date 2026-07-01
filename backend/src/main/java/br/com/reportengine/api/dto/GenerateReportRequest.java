package br.com.reportengine.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record GenerateReportRequest(
        @NotBlank String tpFormatoSaida,
        Map<String, Object> filtros
) {
}
