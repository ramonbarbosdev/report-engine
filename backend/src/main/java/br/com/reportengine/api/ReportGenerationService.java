package br.com.reportengine.api;

import br.com.reportengine.api.dto.GenerateReportRequest;
import br.com.reportengine.core.*;
import br.com.reportengine.domain.entity.ReportDefinitionEntity;
import br.com.reportengine.domain.entity.ReportExecutionLogEntity;
import br.com.reportengine.domain.entity.ReportQueryEntity;
import br.com.reportengine.domain.entity.ReportTemplateEntity;
import br.com.reportengine.domain.enums.OutputFormat;
import br.com.reportengine.domain.repository.ReportExecutionLogRepository;
import br.com.reportengine.domain.service.ReportDefinitionLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportGenerationService {

    private final ReportDefinitionLoader reportDefinitionLoader;
    private final ReportExecutionLogRepository executionLogRepository;
    private final FilterValidationService filterValidationService;
    private final QueryExecutorService queryExecutorService;
    private final JasperReportService jasperReportService;
    private final ObjectMapper objectMapper;

    @Transactional
    public GeneratedReport generate(String cdRelatorio, GenerateReportRequest request, String nmSolicitante) {
        long startedAt = System.currentTimeMillis();
        ReportDefinitionEntity relatorio = reportDefinitionLoader.loadWithDetails(cdRelatorio);
        if (!relatorio.isFlAtivo()) {
            throw ReportEngineException.notFound("Relatorio nao encontrado: " + cdRelatorio);
        }

        OutputFormat format = OutputFormat.fromString(request.tpFormatoSaida());
        Map<String, Object> filtros = filterValidationService.validateAndNormalize(
                relatorio,
                request.filtros()
        );

        ReportQueryEntity mainQuery = relatorio.getQueries().stream()
                .filter(ReportQueryEntity::isFlAtivo)
                .filter(q -> "main".equalsIgnoreCase(q.getNmQuery()))
                .findFirst()
                .orElseThrow(() -> ReportEngineException.badRequest("Query principal nao configurada"));

        ReportTemplateEntity template = relatorio.getTemplates().stream()
                .filter(ReportTemplateEntity::isFlAtivo)
                .findFirst()
                .orElseThrow(() -> ReportEngineException.badRequest("Template ativo nao configurado"));

        try {
            QueryExecutorService.QueryExecutionResult queryResult = queryExecutorService.execute(
                    relatorio.getNmDatasource(),
                    mainQuery.getDsSql(),
                    filtros
            );

            Map<String, Object> jasperParams = toJasperParameters(filtros, nmSolicitante);
            jasperParams.putAll(executeAuxiliaryQueries(relatorio, filtros));

            byte[] content = jasperReportService.render(
                    template.getDsCaminho(),
                    jasperParams,
                    queryResult.rows(),
                    format
            );

            saveLog(relatorio, cdRelatorio, format, filtros, queryResult.nuLinhas(),
                    System.currentTimeMillis() - startedAt, true, null, nmSolicitante);

            String nmArquivo = cdRelatorio + "." + format.getExtension();
            return new GeneratedReport(content, format.getContentType(), nmArquivo);
        } catch (RuntimeException ex) {
            saveLog(relatorio, cdRelatorio, format, filtros, null,
                    System.currentTimeMillis() - startedAt, false, ex.getMessage(), nmSolicitante);
            throw ex;
        }
    }

    private Map<String, Object> toJasperParameters(Map<String, Object> filtros, String nmSolicitante) {
        Map<String, Object> params = new HashMap<>();
        filtros.forEach((key, value) -> {
            if (value instanceof LocalDate date) {
                params.put(key, date.toString());
            } else {
                params.put(key, value);
            }
        });
        if (nmSolicitante != null && !nmSolicitante.isBlank()) {
            params.put("USUARIO_EMISSAO", nmSolicitante);
        }
        return params;
    }

    /**
     * Queries ativas com nmQuery diferente de "main" sao executadas e cada coluna
     * do primeiro registro vira parametro Jasper: {nmQuery}_{nmColuna}.
     */
    private Map<String, Object> executeAuxiliaryQueries(
            ReportDefinitionEntity relatorio,
            Map<String, Object> filtros
    ) {
        Map<String, Object> params = new HashMap<>();
        for (ReportQueryEntity query : relatorio.getQueries()) {
            if (!query.isFlAtivo() || "main".equalsIgnoreCase(query.getNmQuery())) {
                continue;
            }
            QueryExecutorService.QueryExecutionResult result = queryExecutorService.execute(
                    relatorio.getNmDatasource(),
                    query.getDsSql(),
                    filtros
            );
            if (result.rows().isEmpty()) {
                continue;
            }
            String prefix = query.getNmQuery() + "_";
            result.rows().get(0).forEach((column, value) ->
                    params.put(prefix + column, value)
            );
        }
        return params;
    }

    private void saveLog(
            ReportDefinitionEntity relatorio,
            String cdRelatorio,
            OutputFormat format,
            Map<String, Object> filtros,
            Integer nuLinhas,
            long nuDuracaoMs,
            boolean flSucesso,
            String dsErro,
            String nmSolicitante
    ) {
        ReportExecutionLogEntity log = new ReportExecutionLogEntity();
        log.setRelatorio(relatorio);
        log.setCdRelatorio(cdRelatorio);
        log.setTpFormatoSaida(format.name());
        try {
            log.setDsFiltrosJson(objectMapper.writeValueAsString(filtros));
        } catch (Exception ignored) {
            log.setDsFiltrosJson("{}");
        }
        log.setNuLinhas(nuLinhas);
        log.setNuDuracaoMs(nuDuracaoMs);
        log.setFlSucesso(flSucesso);
        log.setDsErro(dsErro);
        log.setNmSolicitante(nmSolicitante);
        executionLogRepository.save(log);
    }

    public record GeneratedReport(byte[] content, String contentType, String nmArquivo) {
    }
}
