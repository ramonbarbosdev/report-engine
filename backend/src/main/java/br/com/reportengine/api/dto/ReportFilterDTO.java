package br.com.reportengine.api.dto;

import br.com.reportengine.domain.enums.FilterType;

public record ReportFilterDTO(
        String nmChave,
        String dsRotulo,
        FilterType tpFiltro,
        boolean flObrigatorio,
        String dsValorPadrao,
        String dsQueryOpcoes,
        int nuOrdem
) {
}
