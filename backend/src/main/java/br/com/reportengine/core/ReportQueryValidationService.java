package br.com.reportengine.core;

import br.com.reportengine.domain.entity.ReportDefinitionEntity;
import br.com.reportengine.domain.entity.ReportFilterEntity;
import br.com.reportengine.domain.entity.ReportQueryEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ReportQueryValidationService {

    private static final Pattern NAMED_PARAM = Pattern.compile("(?<!:):([a-zA-Z_][a-zA-Z0-9_]*)");
    private static final Pattern TRAILING_SEMICOLON = Pattern.compile(";\\s*$");
    private static final Pattern LIMIT_CLAUSE = Pattern.compile("(?i)\\bLIMIT\\s+\\d+");
    private static final Pattern TO_DATE = Pattern.compile("(?i)\\bTO_DATE\\s*\\(");
    private static final Pattern NULLIF_EMPTY = Pattern.compile("(?i)NULLIF\\s*\\([^)]*,\\s*''\\s*\\)");
    private static final Pattern POSITIONAL_PARAM = Pattern.compile("\\?");
    private static final Pattern POSITIONAL_IS_NULL = Pattern.compile("(?i)\\(\\s*\\?\\s+IS\\s+NULL");
    private static final Pattern NAMED_IS_NULL_OR = Pattern.compile("(?i):([a-zA-Z_][a-zA-Z0-9_]*)\\s+IS\\s+NULL\\s+OR");

    public ReportQueryValidationResult validate(String sql, Collection<String> filterKeys) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (sql == null || sql.isBlank()) {
            errors.add("SQL nao pode ficar vazio.");
            return new ReportQueryValidationResult(errors, warnings);
        }

        String normalized = sql.strip();
        Set<String> registeredFilters = filterKeys == null
                ? Set.of()
                : filterKeys.stream()
                        .map(key -> key.toLowerCase(Locale.ROOT))
                        .collect(Collectors.toCollection(LinkedHashSet::new));

        if (TRAILING_SEMICOLON.matcher(normalized).find()) {
            warnings.add(
                    "Remova o ponto e virgula (;) no final do SQL. O motor adiciona LIMIT automaticamente e o ';' pode causar erro de sintaxe."
            );
        }

        if (LIMIT_CLAUSE.matcher(normalized).find()) {
            warnings.add("Nao inclua LIMIT na query. O motor aplica o limite maximo automaticamente.");
        }

        if (TO_DATE.matcher(normalized).find()) {
            errors.add(
                    "Nao use TO_DATE() com parametros opcionais. Prefira COALESCE(:dataInicial, coluna_data) para ignorar o filtro quando vazio."
            );
        }

        if (NULLIF_EMPTY.matcher(normalized).find()) {
            errors.add(
                    "Nao use NULLIF(parametro, ''). Prefira COALESCE(:parametro, coluna) para filtros opcionais."
            );
        }

        if (POSITIONAL_IS_NULL.matcher(normalized).find() || POSITIONAL_PARAM.matcher(normalized).find()) {
            errors.add("Nao use parametros posicionais (?). Use parametros nomeados, por exemplo :idTecnico.");
        }

        Matcher isNullOrMatcher = NAMED_IS_NULL_OR.matcher(normalized);
        while (isNullOrMatcher.find()) {
            String param = isNullOrMatcher.group(1);
            errors.add(
                    "Filtro opcional :" + param + " invalido. Use 'coluna = COALESCE(:" + param + ", coluna)' em vez de ':" + param + " IS NULL OR ...'."
            );
        }

        Set<String> sqlParams = extractNamedParameters(normalized);
        for (String param : sqlParams) {
            if (!registeredFilters.isEmpty() && !registeredFilters.contains(param.toLowerCase(Locale.ROOT))) {
                warnings.add(
                        "Parametro :" + param + " usado no SQL nao esta cadastrado nos filtros do relatorio."
                );
            }
        }

        if (!registeredFilters.isEmpty()) {
            for (String filterKey : registeredFilters) {
                boolean referenced = sqlParams.stream()
                        .anyMatch(param -> param.equalsIgnoreCase(filterKey));
                if (!referenced) {
                    warnings.add(
                            "Filtro '" + filterKey + "' cadastrado, mas nao referenciado no SQL como :" + filterKey + "."
                    );
                }
            }
        }

        return new ReportQueryValidationResult(errors, warnings);
    }

    public ReportQueryValidationResult validateActiveQueries(ReportDefinitionEntity relatorio) {
        List<String> filterKeys = relatorio.getFiltros().stream()
                .map(ReportFilterEntity::getNmChave)
                .toList();

        ReportQueryValidationResult aggregated = ReportQueryValidationResult.empty();
        for (ReportQueryEntity query : relatorio.getQueries()) {
            if (!query.isFlAtivo()) {
                continue;
            }
            ReportQueryValidationResult result = validate(query.getDsSql(), filterKeys);
            aggregated = aggregated.merge(prefixResult(result, query.getNmQuery()));
        }
        return aggregated;
    }

    private ReportQueryValidationResult prefixResult(ReportQueryValidationResult result, String queryName) {
        List<String> errors = result.errors().stream()
                .map(message -> "[" + queryName + "] " + message)
                .toList();
        List<String> warnings = result.warnings().stream()
                .map(message -> "[" + queryName + "] " + message)
                .toList();
        return new ReportQueryValidationResult(errors, warnings);
    }

    private Set<String> extractNamedParameters(String sql) {
        Set<String> params = new LinkedHashSet<>();
        Matcher matcher = NAMED_PARAM.matcher(sql);
        while (matcher.find()) {
            params.add(matcher.group(1));
        }
        return params;
    }
}
