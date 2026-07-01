package br.com.reportengine.core;

import br.com.reportengine.config.ReportEngineProperties;
import lombok.RequiredArgsConstructor;
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

        List<Map<String, Object>> rows = jdbc.query(
                sql + " LIMIT " + properties.getMaxRows(),
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

    public record QueryExecutionResult(List<Map<String, Object>> rows, int nuLinhas) {
    }
}
