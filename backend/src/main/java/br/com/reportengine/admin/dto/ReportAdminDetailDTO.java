package br.com.reportengine.admin.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record ReportAdminDetailDTO(
        Long idRelatorio,
        String cdRelatorio,
        String nmRelatorio,
        String nmCategoria,
        String dsRelatorio,
        String nmDatasource,
        String dsFormatosSaida,
        boolean flAtivo,
        Integer nuVersao,
        OffsetDateTime dtAlteracao,
        List<ReportFilterAdminDTO> filtros,
        List<ReportQueryAdminDTO> queries,
        List<ReportTemplateAdminDTO> templates
) {
}
