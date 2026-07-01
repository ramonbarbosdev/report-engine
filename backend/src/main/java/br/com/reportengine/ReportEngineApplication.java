package br.com.reportengine;

import br.com.reportengine.config.DotenvLoader;
import br.com.reportengine.config.ReportEngineProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ReportEngineProperties.class)
public class ReportEngineApplication {

    public static void main(String[] args) {
        DotenvLoader.load();
        SpringApplication.run(ReportEngineApplication.class, args);
    }
}
