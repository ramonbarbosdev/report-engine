package br.com.reportengine.config;

import io.github.cdimascio.dotenv.Dotenv;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Carrega variaveis do arquivo .env antes do Spring Boot iniciar.
 * Valores ja definidos no SO (System.getenv) ou em System.getProperty nao sao sobrescritos.
 */
public final class DotenvLoader {

    private DotenvLoader() {
    }

    public static void load() {
        Path envDirectory = findEnvDirectory();
        Dotenv dotenv = Dotenv.configure()
                .directory(envDirectory.toString())
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry -> {
            String key = entry.getKey();
            if (!hasExternalValue(key)) {
                System.setProperty(key, entry.getValue());
            }
        });
    }

    private static boolean hasExternalValue(String key) {
        return System.getenv(key) != null || System.getProperty(key) != null;
    }

    private static Path findEnvDirectory() {
        List<Path> candidates = List.of(
                Path.of(".env"),
                Path.of("..", ".env")
        );

        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                Path parent = candidate.getParent();
                return parent != null ? parent : Path.of(".");
            }
        }

        return Path.of(".");
    }
}
