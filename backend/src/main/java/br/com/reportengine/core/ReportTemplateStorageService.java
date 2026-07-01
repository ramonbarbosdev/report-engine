package br.com.reportengine.core;

import br.com.reportengine.config.ReportEngineProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
public class ReportTemplateStorageService {

    private final ReportEngineProperties properties;

    public Path resolve(String relativePath) {
        return Path.of(properties.getTemplatesPath()).resolve(relativePath).normalize();
    }

    public String store(String cdRelatorio, int nuVersao, MultipartFile file) {
        try {
            String nmArquivo = file.getOriginalFilename() == null ? "template.jrxml" : file.getOriginalFilename();
            String relativePath = cdRelatorio + "/v" + nuVersao + "/" + nmArquivo;
            Path target = resolve(relativePath);
            Files.createDirectories(target.getParent());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return relativePath;
        } catch (IOException ex) {
            throw new IllegalStateException("Falha ao salvar template", ex);
        }
    }
}
