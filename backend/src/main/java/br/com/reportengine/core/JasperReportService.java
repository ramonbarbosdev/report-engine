package br.com.reportengine.core;

import br.com.reportengine.domain.enums.OutputFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class JasperReportService {

    private final ReportTemplateStorageService templateStorage;
    private final Map<String, CacheEntry> compiledCache = new HashMap<>();

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

            JasperReport jasperReport = resolveCompiled(templateRelativePath, templatePath);

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
        if (templateRelativePath != null && !templateRelativePath.isBlank()) {
            compiledCache.remove(templateRelativePath);
        }
    }

    public void evictAll(java.util.Collection<String> templateRelativePaths) {
        if (templateRelativePaths == null) {
            return;
        }
        templateRelativePaths.forEach(this::evictCache);
    }

    private JasperReport resolveCompiled(String templateRelativePath, Path templatePath) {
        try {
            long lastModified = Files.getLastModifiedTime(templatePath).toMillis();
            CacheEntry cached = compiledCache.get(templateRelativePath);
            if (cached != null && cached.lastModified == lastModified) {
                return cached.report;
            }

            log.info("Compilando template JRXML: {} (modificado em {})", templateRelativePath, lastModified);
            JasperReport report = compile(templatePath);
            compiledCache.put(templateRelativePath, new CacheEntry(report, lastModified));
            return report;
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao ler template: " + templatePath, ex);
        }
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

    private record CacheEntry(JasperReport report, long lastModified) {
    }
}
