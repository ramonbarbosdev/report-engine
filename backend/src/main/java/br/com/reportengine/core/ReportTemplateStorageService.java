package br.com.reportengine.core;

import br.com.reportengine.config.ReportEngineProperties;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportTemplateStorageService {

    private final ReportEngineProperties properties;

    @Getter
    private Path basePath;

    @PostConstruct
    void initialize() {
        this.basePath = locateBasePath(properties.getTemplatesPath());
        log.info("Pasta de templates JRXML: {}", basePath.toAbsolutePath());
    }

    public Path resolve(String relativePath) {
        String normalized = relativePath.replace('\\', '/');
        return basePath.resolve(normalized).normalize();
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

    public void delete(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        try {
            Path file = resolve(relativePath);
            Files.deleteIfExists(file);
            Path versionDir = file.getParent();
            if (versionDir != null && Files.isDirectory(versionDir)) {
                try (var entries = Files.list(versionDir)) {
                    if (entries.findAny().isEmpty()) {
                        Files.deleteIfExists(versionDir);
                    }
                }
            }
        } catch (IOException ex) {
            log.warn("Nao foi possivel remover arquivo de template {}: {}", relativePath, ex.getMessage());
        }
    }

    private Path locateBasePath(String configured) {
        Set<Path> candidates = new LinkedHashSet<>();
        Path configuredPath = Path.of(configured);
        Path userDir = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();

        Path discovered = discoverDataTemplatesDir(userDir);
        if (discovered != null) {
            candidates.add(discovered);
        }

        if (configuredPath.isAbsolute()) {
            candidates.add(configuredPath.normalize());
        } else {
            candidates.add(userDir.resolve(configuredPath).normalize());
        }

        for (Path candidate : candidates) {
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
        }

        Path fallback = candidates.iterator().next();
        try {
            Files.createDirectories(fallback);
            log.warn("Diretorio de templates criado em: {}", fallback.toAbsolutePath());
        } catch (IOException ex) {
            throw new IllegalStateException("Nao foi possivel criar diretorio de templates: " + fallback, ex);
        }
        return fallback;
    }

    private Path discoverDataTemplatesDir(Path start) {
        Path current = start;
        for (int depth = 0; depth < 6 && current != null; depth++) {
            Path candidate = current.resolve("data/templates");
            if (Files.isDirectory(candidate)) {
                return candidate.normalize();
            }
            current = current.getParent();
        }
        return null;
    }
}
