package br.com.reportengine.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class DatasourceConfig {

    private final ReportEngineProperties properties;

    @Bean
    public Map<String, DataSource> reportDataSources() {
        Map<String, DataSource> dataSources = new HashMap<>();
        properties.getDatasources().forEach((key, config) -> {
            HikariConfig hikari = new HikariConfig();
            hikari.setJdbcUrl(config.getUrl());
            hikari.setUsername(config.getUsername());
            hikari.setPassword(config.getPassword());
            hikari.setReadOnly(config.isReadOnly());
            hikari.setPoolName("report-" + key);
            hikari.setMaximumPoolSize(5);
            dataSources.put(key, new HikariDataSource(hikari));
        });
        return dataSources;
    }
}
