package co.edu.unal.migration.commerceanalyzer.config;

import co.edu.unal.migration.commerceanalyzer.constants.CommerceAnalyzerConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

@Configuration
@PropertySource("classpath:application.properties")
public class ApplicationConfig {

    @Bean
    public Properties getApplicationProperties() throws IOException {
        Resource resource = new ClassPathResource(CommerceAnalyzerConstants.APPLICATION_PROPERTIES);
        Properties props = PropertiesLoaderUtils.loadProperties(resource);
        return props;
    }
}
