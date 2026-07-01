package br.com.reportengine.core;

import br.com.reportengine.domain.enums.OutputFormat;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JasperReportService {

    private final ReportTemplateStorageService templateStorage;
    private final Map<String, JasperReport> compiledCache = new HashMap<>();

    public byte[] render(
            String templateRelativePath,
            Map<String, Object> parameters,
            Collection<Map<String, Object>> rows,
            OutputFormat format
    ) {
        try {
            Path templatePath = templateStorage.resolve(templateRelativePath);
            if (!Files.exists(templatePath)) {
                throw ReportEngineException.badRequest("Template nao encontrado: " + templateRelativePath);
            }

            JasperReport jasperReport = compiledCache.computeIfAbsent(
                    templateRelativePath,
                    key -> compile(templatePath)
            );

            @SuppressWarnings("unchecked")
            Collection<Map<String, ?>> dataRows = (Collection) rows;
            JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(dataRows);
            JasperPrint print = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            return switch (format) {
                case PDF -> JasperExportManager.exportReportToPdf(print);
                case XLSX -> exportXlsx(print);
            };
        } catch (JRException ex) {
            throw new IllegalStateException("Falha ao gerar relatorio Jasper: " + ex.getMessage(), ex);
        }
    }

    public void evictCache(String templateRelativePath) {
        compiledCache.remove(templateRelativePath);
    }

    private JasperReport compile(Path templatePath) {
        try (InputStream input = Files.newInputStream(templatePath)) {
            return JasperCompileManager.compileReport(input);
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao compilar template: " + templatePath, ex);
        }
    }

    private byte[] exportXlsx(JasperPrint print) throws JRException {
        JRXlsxExporter exporter = new JRXlsxExporter();
        exporter.setExporterInput(new SimpleExporterInput(print));
        try (var output = new java.io.ByteArrayOutputStream()) {
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(output));
            exporter.exportReport();
            return output.toByteArray();
        } catch (java.io.IOException ex) {
            throw new JRException(ex);
        }
    }
}
