package br.com.reportengine.admin.dto;

public record ReportSummaryAdminDTO(
        Long idRelatorio,
        String cdRelatorio,
        String nmRelatorio,
        String nmCategoria,
        boolean flAtivo,
        Integer nuVersao
) {
}
