package br.com.reportengine.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "report-engine")
public class ReportEngineProperties {

    private String apiKey;
    private String templatesPath = "data/templates";
    private int maxRows = 50_000;
    private Map<String, DatasourceProperties> datasources = new LinkedHashMap<>();

    @Getter
    @Setter
    public static class DatasourceProperties {
        private String url;
        private String username;
        private String password;
        private boolean readOnly = true;
    }
}
