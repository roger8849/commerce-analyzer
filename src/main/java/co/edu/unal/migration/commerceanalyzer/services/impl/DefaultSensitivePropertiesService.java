package co.edu.unal.migration.commerceanalyzer.services.impl;

import co.edu.unal.migration.commerceanalyzer.config.ApplicationConfig;
import co.edu.unal.migration.commerceanalyzer.services.SensitivePropertiesService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class DefaultSensitivePropertiesService implements SensitivePropertiesService {
    @Autowired
    private ApplicationConfig applicationConfig;

    @Override
    public void generateSensitiveFiles() throws IOException {
        this.generateSensitivePropertyFile("p1");
        this.generateSensitivePropertyFile("s1");
        this.generateSensitivePropertyFile("d1");

        List<String> environmentList = new ArrayList<>();
        environmentList.add("d1");
        environmentList.add("s1");
        environmentList.add("p1");
    }

    public void generateSensitivePropertyFile(String environmentName) throws IOException {
        File configFile = getPropertyFile(environmentName);

        if (configFile != null) {
            List<String> duplicateProperties = checkDuplicate(configFile);
            Map<String, String> propertiesMap = loadPropertyFile(configFile);

            if (duplicateProperties.size() > 0) {
                System.out.println("Duplicate Properties exists in " + environmentName + " prop file");
                //printList(duplicateProperties);
            }
            Map<String, String> sensitivePropertiesMap = filterSensitiveProperties(propertiesMap);
            List<String> sensitiveProperty = getLineToWriteFromProperties(sensitivePropertiesMap, environmentName);
            writeToFile(sensitiveProperty, environmentName, true);

            createNewPropertyFileForEnvironment(sensitiveProperty, environmentName);
            generateZipFile(environmentName);
            System.out.println("Total Properties in " + environmentName + " Config:" + propertiesMap.size());
            System.out.println("Total Sensitive Properties in " + environmentName + ":" + sensitiveProperty.size());
        } else {
            System.out.println("No property file found for environment:" + environmentName);
        }
    }

    private void generateZipFile(String environmentName) {
        ZipOutputStream zout;
        try (FileOutputStream fout = new FileOutputStream("./output/sensitiveZipFile/Petco-" + environmentName + "_properties_hcs_platform_common.zip")) {
            zout = new ZipOutputStream(fout);
            ZipEntry ze = new ZipEntry("./output/Petco-" + environmentName + "_properties_hcs_platform_common.properties");
            zout.putNextEntry(ze);
            zout.finish();
            zout.closeEntry();
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createNewPropertyFileForEnvironment(List<String> sensitiveProperty, String environmentName) throws IOException {

        List<String> propertyFileLines = getFileLines(getPropertyFile(environmentName), false);

        propertyFileLines.removeAll(sensitiveProperty);

        List<String> managedProperties = getFileLines(new File("managed.properties"), false);

        List<String> propertyFileLinesCopy = new ArrayList<>();
        propertyFileLinesCopy.addAll(propertyFileLines);

        propertyFileLines.forEach(s -> {
            if (managedProperties.contains(s.split("=")[0].trim())) {
                propertyFileLinesCopy.set(propertyFileLinesCopy.indexOf(s), "#" + s);
            }
        });
        writeToFile(propertyFileLinesCopy, environmentName, false);
    }


    private void writeToFile(List<String> lines, String environmentName, boolean isSensitivePropertyFile) {

        File propertyFile;
        if (isSensitivePropertyFile) {
            propertyFile = new File("./output/Petco-" + environmentName + "_properties_hcs_platform_common.properties");
        } else {
            propertyFile = new File("./output/" + environmentName + "/local.properties");
        }
        try {
            if (propertyFile.exists()) {
                propertyFile.delete();
                propertyFile.createNewFile();
            }
            FileUtils.writeLines(propertyFile, lines);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> getCommonProperties(Map<String, String> propertyMap1, Map<String, String> propertyMap2) {
        Map<String, String> commonProperty = new HashMap<>();
        for (String property : propertyMap1.keySet()) {
            if (propertyMap2.containsKey(property) && propertyMap1.get(property).equalsIgnoreCase(propertyMap2.get(property))) {
                commonProperty.put(property, propertyMap1.get(property));
            }
        }
        return commonProperty;
    }


    private List<String> checkDuplicate(File f) {
        List<String> duplicateProperties = new ArrayList<>();
        List<String> strings = null;


        strings = getFileLines(f, true);
        final Map<String, String> properties = new LinkedHashMap<String, String>();

        strings.stream().forEach(line -> {
            String[] splitValue = line.split("=");
            if (properties.containsKey(splitValue[0])) {
                duplicateProperties.add(splitValue[0] + "=" + properties.get(splitValue[0]));
                duplicateProperties.add(line);

            } else {
                if (splitValue.length == 1 || StringUtils.isBlank(splitValue[1])) {
                    properties.put(splitValue[0], StringUtils.EMPTY);
                } else {
                    properties.put(splitValue[0], splitValue[1]);
                }
            }

        });
        return duplicateProperties;

    }

    private Map<String, String> filterSensitiveProperties(Map<String, String> propertiesMap) {
        Map<String, String> sensitivePropertyMap = new LinkedHashMap<>();
        List<String> sensitivePropertiesSuffixes = Arrays.asList(".userid", ".username", ".password", ".user", ".id", ".key", ".pwd");
        for (String property : propertiesMap.keySet()) {
            if (sensitivePropertiesSuffixes.stream().anyMatch(suffix -> StringUtils.endsWithIgnoreCase(property, suffix))) {
                sensitivePropertyMap.put(property, propertiesMap.get(property));
            }
        }
        return sensitivePropertyMap;
    }

    private List<String> getLineToWriteFromProperties(Map<String, String> sensitivePropertiesMap, String environmentName) throws IOException {
        List<String> propertyFileLine = this.getFileLines(this.getPropertyFile(environmentName), true);

        List<String> sensitiveProperties = new LinkedList<>();
        List<String> sensitivePropertiesSuffixes = Arrays.asList(".password", ".key.pwd", ".pwd");
        for (String line : propertyFileLine) {
            String property = line.split("=")[0].trim();
            if (sensitivePropertiesMap.containsKey(property)) {
                sensitiveProperties.add(line);
                if (sensitivePropertiesSuffixes.stream().anyMatch(suffix -> StringUtils.endsWithIgnoreCase(property, suffix))) {
                    // sensitiveProperties.add("\n");
                }
            }
        }
        return sensitiveProperties;
    }


    private void printMap(Map<String, String> propertiesMap) {
        for (String property : propertiesMap.keySet()) {
            System.out.println(property + ":" + propertiesMap.get(property));
        }
    }

    private void printList(List<String> propertyList) {
        for (String property : propertyList) {
            System.out.println(property);
        }

    }

    private List<String> getFileLines(File f, boolean skipBlank) {
        List<String> strings = new ArrayList<String>();
        try {
            strings = FileUtils.readLines(f, "UTF-8");
            if (skipBlank) {
                strings = strings.stream().filter(s -> StringUtils.isNotBlank(s) && !StringUtils.contains(s, "#")).collect(Collectors.toList());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strings;
    }

    private File getPropertyFile(String environmentName) throws IOException {
        File configFile = null;
        Properties configProperties = applicationConfig.getApplicationProperties();
        if (configProperties.containsKey(environmentName + ".local.properties")) {
            configFile = new File(configProperties.get(environmentName + ".local.properties").toString());
        }
        return configFile;
    }

    private Map<String, String> loadPropertyFile(File f) {
        Properties prop = new Properties();

        Map<String, String> map = null;
        try {
            InputStream is = new FileInputStream(f);
            prop.load(is);
            map = (Map) prop;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    public void compareDifference(Map<String, String> configMap, Map<String, String> cloudConfigMap) {
        List<String> notExists = new ArrayList<String>();
        List<String> differentValues = new ArrayList<String>();
        configMap.keySet().forEach(key -> {
            if (!cloudConfigMap.containsKey(key)) {
                notExists.add(key);
            } else {
                if (!configMap.get(key).trim().equalsIgnoreCase(cloudConfigMap.get(key).trim())) {
                    differentValues.add(key);
                }
            }
        });

        List<String> sortedNotExists = notExists.stream().sorted().collect(Collectors.toList());
        System.out.println("\nFollowing keys don't exists \n");
        sortedNotExists.forEach(key -> System.out.println("" + key + " " + configMap.get(key)));

        System.out.println("\nFollowing keys are different \n");
        differentValues.forEach(key -> System.out.println("Key :" + key + " Config Property Value :" + configMap.get(key) + ",Cloud Config Property Value :" + cloudConfigMap.get(key)));
    }

}
