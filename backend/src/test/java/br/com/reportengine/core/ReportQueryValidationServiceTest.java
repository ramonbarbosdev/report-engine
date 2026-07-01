package br.com.reportengine.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportQueryValidationServiceTest {

    private final ReportQueryValidationService service = new ReportQueryValidationService();

    @Test
    void rejectsTrailingSemicolonAndToDate() {
        String sql = """
                SELECT 1 FROM projeto p
                WHERE p.dt_projeto >= TO_DATE(:dataInicial, 'YYYY-MM-DD');
                """;

        ReportQueryValidationResult result = service.validate(sql, List.of("dataInicial"));

        assertTrue(result.hasErrors());
        assertTrue(result.hasWarnings());
        assertTrue(result.errors().stream().anyMatch(message -> message.contains("TO_DATE")));
        assertTrue(result.warnings().stream().anyMatch(message -> message.contains("ponto e virgula")));
    }

    @Test
    void rejectsPositionalParameters() {
        String sql = """
                SELECT 1 FROM tecnico t
                WHERE (? IS NULL OR t.id_tecnico = ?)
                """;

        ReportQueryValidationResult result = service.validate(sql, List.of("idTecnico"));

        assertTrue(result.hasErrors());
        assertTrue(result.errors().stream().anyMatch(message -> message.contains("posicionais")));
    }

    @Test
    void rejectsLegacyIsNullOrPattern() {
        String sql = """
                SELECT 1 FROM tecnico t
                WHERE :idTecnico IS NULL OR t.id_tecnico = :idTecnico
                """;

        ReportQueryValidationResult result = service.validate(sql, List.of("idTecnico"));

        assertTrue(result.hasErrors());
        assertTrue(result.errors().stream().anyMatch(message -> message.contains("COALESCE")));
    }

    @Test
    void acceptsCoalescePattern() {
        String sql = """
                SELECT 1 FROM projeto p
                WHERE p.dt_projeto >= COALESCE(:dataInicial, p.dt_projeto)
                ORDER BY p.nm_projeto
                """;

        ReportQueryValidationResult result = service.validate(sql, List.of("dataInicial"));

        assertFalse(result.hasErrors());
        assertFalse(result.hasWarnings());
    }
}
