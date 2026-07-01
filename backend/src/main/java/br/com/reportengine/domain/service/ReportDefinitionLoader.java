package br.com.reportengine.domain.service;

import br.com.reportengine.core.ReportEngineException;
import br.com.reportengine.domain.entity.ReportDefinitionEntity;
import br.com.reportengine.domain.repository.ReportDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportDefinitionLoader {

    private final ReportDefinitionRepository reportRepository;

    @Transactional(readOnly = true)
    public ReportDefinitionEntity loadWithDetails(String cdRelatorio) {
        ReportDefinitionEntity relatorio = reportRepository.findByCdRelatorio(cdRelatorio)
                .orElseThrow(() -> ReportEngineException.notFound("Relatorio nao encontrado: " + cdRelatorio));

        relatorio.getFiltros().size();
        relatorio.getQueries().size();
        relatorio.getTemplates().size();

        return relatorio;
    }
}
