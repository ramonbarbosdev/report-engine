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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
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
                .max(Comparator.comparing(ReportTemplateEntity::getNuVersao))
                .orElseThrow(() -> ReportEngineException.badRequest("Template ativo nao configurado"));

        log.info(
                "Gerando relatorio {} usando template v{} ({})",
                cdRelatorio,
                template.getNuVersao(),
                template.getDsCaminho()
        );

        try {
            QueryExecutorService.QueryExecutionResult queryResult = queryExecutorService.execute(
                    relatorio.getNmDatasource(),
                    mainQuery.getDsSql(),
                    filtros
            );

            Map<String, Object> jasperParams = toJasperParameters(filtros, nmSolicitante);
            List<Map<String, Object>> rows = mergeAuxiliaryFieldsIntoRows(
                    relatorio,
                    filtros,
                    queryResult.rows()
            );

            byte[] content = jasperReportService.render(
                    template.getDsCaminho(),
                    jasperParams,
                    rows,
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
            } else if (value instanceof Number number) {
                params.put(key, toJasperNumber(number));
            } else {
                params.put(key, value);
            }
        });
        if (nmSolicitante != null && !nmSolicitante.isBlank()) {
            params.put("USUARIO_EMISSAO", nmSolicitante);
        }
        return params;
    }

    private Object toJasperNumber(Number number) {
        if (number instanceof Double || number instanceof Float) {
            double d = number.doubleValue();
            if (d == Math.rint(d) && d >= Long.MIN_VALUE && d <= Long.MAX_VALUE) {
                return (long) d;
            }
            return d;
        }
        return number.longValue();
    }

    /**
     * Queries ativas com nmQuery diferente de "main" sao executadas e cada coluna
     * do primeiro registro e injetada como field em todas as linhas do detalhe
     * (alias SQL da coluna). Se houver colisao entre queries auxiliares, usa
     * {nmQuery}_{nmColuna}.
     */
    private List<Map<String, Object>> mergeAuxiliaryFieldsIntoRows(
            ReportDefinitionEntity relatorio,
            Map<String, Object> filtros,
            List<Map<String, Object>> mainRows
    ) {
        Map<String, Object> auxiliaryFields = collectAuxiliaryFields(relatorio, filtros);
        List<Map<String, Object>> rows = new ArrayList<>(mainRows);

        if (auxiliaryFields.isEmpty()) {
            return rows;
        }

        if (rows.isEmpty()) {
            rows.add(new LinkedHashMap<>(auxiliaryFields));
            return rows;
        }

        List<Map<String, Object>> mergedRows = new ArrayList<>(rows.size());
        for (Map<String, Object> mainRow : rows) {
            Map<String, Object> row = new LinkedHashMap<>(mainRow);
            row.putAll(auxiliaryFields);
            mergedRows.add(row);
        }
        return mergedRows;
    }

    private Map<String, Object> collectAuxiliaryFields(
            ReportDefinitionEntity relatorio,
            Map<String, Object> filtros
    ) {
        Map<String, Object> fields = new LinkedHashMap<>();
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
                log.warn("Query auxiliar '{}' nao retornou dados para o relatorio {}", query.getNmQuery(), relatorio.getCdRelatorio());
                continue;
            }
            String prefix = query.getNmQuery() + "_";
            result.rows().get(0).forEach((column, value) -> {
                String fieldName = fields.containsKey(column) ? prefix + column : column;
                fields.put(fieldName, normalizeAuxiliaryValue(fieldName, value));
            });
        }
        if (!fields.isEmpty()) {
            log.debug("Fields auxiliares do relatorio {}: {}", relatorio.getCdRelatorio(), fields.keySet());
        }
        return fields;
    }

    /**
     * Garante tipos compativeis com os fields declarados no JRXML (Long / Double).
     */
    private Object normalizeAuxiliaryValue(String fieldName, Object value) {
        if (value == null) {
            return null;
        }
        if (isQuantityField(fieldName)) {
            if (value instanceof Long longValue) {
                return longValue;
            }
            if (value instanceof Number number) {
                return number.longValue();
            }
            return value;
        }
        if (value instanceof Double doubleValue) {
            return doubleValue;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return value;
    }

    private boolean isQuantityField(String fieldName) {
        return "quantidade".equals(fieldName) || fieldName.endsWith("_quantidade");
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
