package br.com.reportengine.admin.dto;

import br.com.reportengine.domain.enums.FilterType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReportFilterUpsertRequest(
        @NotBlank String nmChave,
        @NotBlank String dsRotulo,
        @NotNull FilterType tpFiltro,
        boolean flObrigatorio,
        String dsValorPadrao,
        String dsQueryOpcoes,
        int nuOrdem
) {
}
