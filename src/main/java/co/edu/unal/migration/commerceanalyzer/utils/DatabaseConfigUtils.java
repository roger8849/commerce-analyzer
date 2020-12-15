package co.edu.unal.migration.commerceanalyzer.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

import static co.edu.unal.migration.commerceanalyzer.constants.CommerceAnalyzerConstants.*;


public class DatabaseConfigUtils {

    public static Properties getDatabaseProperties(String databaseName) throws IOException {
        String databaseResourceName;
        if (StringUtils.equalsIgnoreCase(databaseName, ORACLE)) {
            databaseResourceName = ORACLE_PROPERTIES;
        } else if (StringUtils.equalsIgnoreCase(databaseName, MYSQL)) {
            databaseResourceName = MYSQL_PROPERTIES;
        } else {
            databaseResourceName = StringUtils.EMPTY;
        }
        Resource resource = new ClassPathResource(databaseResourceName);
        Properties props = PropertiesLoaderUtils.loadProperties(resource);
        return props;
    }
}
