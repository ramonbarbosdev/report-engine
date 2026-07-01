package br.com.reportengine.core;

import br.com.reportengine.config.ReportEngineProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QueryExecutorService {

    private final Map<String, DataSource> reportDataSources;
    private final ReportEngineProperties properties;

    public QueryExecutionResult execute(
            String nmDatasource,
            String sql,
            Map<String, Object> parameters
    ) {
        DataSource dataSource = reportDataSources.get(nmDatasource);
        if (dataSource == null) {
            throw ReportEngineException.badRequest("Datasource nao configurado: " + nmDatasource);
        }

        NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(dataSource);
        Map<String, Object> safeParams = sanitizeParameters(parameters);
        String executableSql = prepareSql(sql);

        try {
            List<Map<String, Object>> rows = jdbc.query(
                    executableSql + " LIMIT " + properties.getMaxRows(),
                    safeParams,
                    (rs, rowNum) -> {
                        var meta = rs.getMetaData();
                        Map<String, Object> row = new LinkedHashMap<>();
                        for (int i = 1; i <= meta.getColumnCount(); i++) {
                            row.put(meta.getColumnLabel(i), rs.getObject(i));
                        }
                        return row;
                    }
            );

            return new QueryExecutionResult(new ArrayList<>(rows), rows.size());
        } catch (DataAccessException ex) {
            String cause = ex.getMostSpecificCause().getMessage();
            List<String> hints = new ArrayList<>();
            if (cause != null) {
                if (cause.contains("?") || cause.toLowerCase().contains("bad sql grammar")) {
                    hints.add("Revise a query no admin: use parametros nomeados (:idTecnico) e COALESCE para filtros opcionais.");
                }
                if (cause.toLowerCase().contains("invalid input syntax for type date")) {
                    hints.add("Nao use TO_DATE ou NULLIF com string vazia. Use COALESCE(:dataInicial, coluna_data).");
                }
            }
            List<String> errors = new ArrayList<>();
            errors.add(cause == null ? "Falha ao executar SQL do relatorio." : cause);
            errors.addAll(hints);
            throw new ReportValidationException(
                    "Erro ao executar SQL do relatorio. " + (cause == null ? "" : cause),
                    errors,
                    List.of()
            );
        }
    }

    private Map<String, Object> sanitizeParameters(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> safe = new LinkedHashMap<>();
        parameters.forEach((key, value) -> {
            if (value instanceof String text && text.isBlank()) {
                safe.put(key, null);
            } else {
                safe.put(key, value);
            }
        });
        return safe;
    }

    private String prepareSql(String sql) {
        if (sql == null) {
            return "";
        }
        return sql.strip().replaceAll(";\\s*$", "");
    }

    public record QueryExecutionResult(List<Map<String, Object>> rows, int nuLinhas) {
    }
}
