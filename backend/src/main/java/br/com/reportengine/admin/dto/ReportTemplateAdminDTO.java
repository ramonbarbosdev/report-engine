package br.com.reportengine.admin.dto;

import java.time.OffsetDateTime;

public record ReportTemplateAdminDTO(
        Long idRelatoriotemplate,
        Integer nuVersao,
        String nmArquivo,
        boolean flAtivo,
        OffsetDateTime dtCadastro
) {
}
