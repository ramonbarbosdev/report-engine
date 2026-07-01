package br.com.reportengine.admin.dto;

import br.com.reportengine.domain.enums.FilterType;

public record ReportFilterAdminDTO(
        Long idRelatoriofiltro,
        String nmChave,
        String dsRotulo,
        FilterType tpFiltro,
        boolean flObrigatorio,
        String dsValorPadrao,
        String dsQueryOpcoes,
        int nuOrdem
) {
}
