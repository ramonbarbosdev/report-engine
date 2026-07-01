package br.com.reportengine.admin.dto;

import java.util.List;

public record ReportQueryValidationDTO(
        boolean valid,
        List<String> errors,
        List<String> warnings
) {
}
