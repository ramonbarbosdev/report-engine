package br.com.reportengine.admin.dto;

public record ReportQueryAdminDTO(
        Long idRelatorioquery,
        String nmQuery,
        String dsSql,
        boolean flAtivo
) {
}
