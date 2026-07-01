package br.com.reportengine.api;

import br.com.reportengine.api.dto.ReportDefinitionDTO;
import br.com.reportengine.api.dto.ReportFilterDTO;
import br.com.reportengine.api.dto.ReportSummaryDTO;
import br.com.reportengine.core.ReportEngineException;
import br.com.reportengine.domain.entity.ReportDefinitionEntity;
import br.com.reportengine.domain.entity.ReportFilterEntity;
import br.com.reportengine.domain.repository.ReportDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportCatalogService {

    private final ReportDefinitionRepository reportRepository;

    @Transactional(readOnly = true)
    public List<ReportSummaryDTO> listActiveReports() {
        return reportRepository.findByFlAtivoTrueOrderByNmRelatorioAsc().stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReportDefinitionDTO getDefinition(String cdRelatorio) {
        ReportDefinitionEntity relatorio = reportRepository.findWithDetailsByCdRelatorio(cdRelatorio)
                .filter(ReportDefinitionEntity::isFlAtivo)
                .orElseThrow(() -> ReportEngineException.notFound("Relatorio nao encontrado: " + cdRelatorio));
        return toDefinition(relatorio);
    }

    private ReportSummaryDTO toSummary(ReportDefinitionEntity relatorio) {
        return new ReportSummaryDTO(
                relatorio.getCdRelatorio(),
                relatorio.getNmRelatorio(),
                relatorio.getNmCategoria(),
                relatorio.isFlAtivo(),
                parseFormats(relatorio.getDsFormatosSaida())
        );
    }

    private ReportDefinitionDTO toDefinition(ReportDefinitionEntity relatorio) {
        List<ReportFilterDTO> filtros = relatorio.getFiltros().stream()
                .map(this::toFilter)
                .toList();
        return new ReportDefinitionDTO(
                relatorio.getCdRelatorio(),
                relatorio.getNmRelatorio(),
                relatorio.getNmCategoria(),
                relatorio.getDsRelatorio(),
                parseFormats(relatorio.getDsFormatosSaida()),
                filtros
        );
    }

    private ReportFilterDTO toFilter(ReportFilterEntity filtro) {
        return new ReportFilterDTO(
                filtro.getNmChave(),
                filtro.getDsRotulo(),
                filtro.getTpFiltro(),
                filtro.isFlObrigatorio(),
                filtro.getDsValorPadrao(),
                filtro.getDsQueryOpcoes(),
                filtro.getNuOrdem()
        );
    }

    private List<String> parseFormats(String formats) {
        return Arrays.stream(formats.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}
