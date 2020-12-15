package co.edu.unal.migration.commerceanalyzer.services.impl;

import co.edu.unal.migration.commerceanalyzer.utils.DatabaseConfigUtils;
import co.edu.unal.migration.commerceanalyzer.dto.WordCrawlerInputParams;
import co.edu.unal.migration.commerceanalyzer.dto.WordFound;
import co.edu.unal.migration.commerceanalyzer.services.WordCrawlerService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;

import static co.edu.unal.migration.commerceanalyzer.constants.CommerceAnalyzerConstants.*;

@Service
public class DefaultWordCrawlerService implements WordCrawlerService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultWordCrawlerService.class);

    @Autowired
    private Environment environment;

    @Override
    public Set<WordFound> findOracleKeysInJavaFiles(WordCrawlerInputParams wordCrawlerInputParams) throws IOException {
        Set<WordFound> wordFoundSet = new HashSet<>();
        Set<String> javaFilesPaths = this.listJavaFilesFromRootDirectory();
        for (String javaFilePath : javaFilesPaths) {
            WordFound wordFound = new WordFound();
            wordFound.setFilePath(javaFilePath);
            wordFound = this.searchInJavaFile(wordFound, new File(javaFilePath), wordCrawlerInputParams.getShouldReplaceText());
            if (MapUtils.isNotEmpty(wordFound.getWordsFound())) {
                wordFoundSet.add(wordFound);
            }
        }
        return wordFoundSet;
    }

    @Override
    public Set<WordFound> searchWordInDirectory(Set<WordFound> wordFoundSet, String searchWord) {
        File directory = new File(environment.getProperty(ROOT_DIRECTORY_CONFIG_KEY));
        if (Objects.isNull(directory) || !directory.exists()) {
            System.out.println("Directory doesn't exists!!!");
            return null;
        }
        Set<String> javaFilesPaths = this.listJavaFilesFromRootDirectory();
        for (String filePath : javaFilesPaths) {
            File javaFile = new File(filePath);
            WordFound wordFound = this.findWord(javaFile, searchWord);
            if (Objects.nonNull(wordFound)) {
                wordFoundSet.add(wordFound);
            }
        }
        return wordFoundSet;
    }

    @Override
    public Set<String> listJavaFilesFromRootDirectory() {
        Set<String> javaFiles = new HashSet<>();
        File rootDirectory = new File(environment.getProperty(ROOT_DIRECTORY_CONFIG_KEY));
        this.listJavaFiles(javaFiles, rootDirectory);
        return javaFiles;
    }

    private void listJavaFiles(Set<String> javaFiles, File directory) {
        File[] filesAndDirs = directory.listFiles();
        if (Objects.nonNull(filesAndDirs) && filesAndDirs.length > 0) {
            for (File file : filesAndDirs) {
                if (file.isFile()) {
                    if (StringUtils.endsWithIgnoreCase(file.getName(), environment.getProperty(FILENAME_EXTENSION))) {
                        javaFiles.add(file.getAbsolutePath());
                    }
                } else {
                    listJavaFiles(javaFiles, file);
                }
            }
        }
    }

    /**
     * @param file
     * @param searchWord
     * @return
     */
    private WordFound findWord(File file, String searchWord) {
        WordFound wordFound = null;
        Scanner scanFile;
        final String searchRegex = new StringBuilder("(?i)\\b").append(searchWord).append("\\b").toString();
        try {
            scanFile = new Scanner(file);
            List<Integer> wordOccurenceIndex = new ArrayList<>();
            while (Objects.nonNull(scanFile.findWithinHorizon(searchRegex, 0))) {
                MatchResult mr = scanFile.match();
                if (Objects.isNull(wordFound)) {
                    wordFound = new WordFound();
                }
                wordFound.setFilePath(file.getAbsolutePath());
                if (MapUtils.isEmpty(wordFound.getWordsFound())) {
                    wordFound.setWordsFound(new HashMap<>());
                }
                wordOccurenceIndex.add(mr.start());
                wordFound.getWordsFound().put(searchWord, wordOccurenceIndex);
            }
            scanFile.close();
        } catch (FileNotFoundException e) {
            LOG.error("Search File Not Found !!!!! ");
        }
        return wordFound;
    }

    private WordFound searchInJavaFile(WordFound wordFound, File javaFile, boolean shouldReplaceLines) throws IOException {
        Path javaPathFile = Paths.get(javaFile.getAbsolutePath());
        List<String> lines = Files.readAllLines(javaPathFile);
        List<String> replacedLines = null;
        if (shouldReplaceLines) {
            replacedLines = new ArrayList<>();
        }
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Map<String, List<Integer>> occurrences = wordFound.getWordsFound();
            if (MapUtils.isEmpty(occurrences)) {
                occurrences = new HashMap<>();
            }
            occurrences = searchOracleKeysInLine(occurrences, line, i + 1, replacedLines);
            wordFound.setWordsFound(occurrences);
        }
        if (shouldReplaceLines && !CollectionUtils.isEqualCollection(lines, replacedLines)) {
//        if (wasReplaced) {
            Files.write(javaPathFile, replacedLines, Charset.forName("UTF-8"));
        }
        return wordFound;
    }

    private Map<String, List<Integer>> searchOracleKeysInLine(Map<String, List<Integer>> occurrences,
                                                              String line,
                                                              Integer lineNumber, List<String> replacedLines) throws IOException {
        Properties databaseProperties = DatabaseConfigUtils.getDatabaseProperties(ORACLE);
        Set<String> oracleKeys = databaseProperties.keySet().stream().map(Object::toString).collect(Collectors.toSet());
        String replacedLine = line;
        boolean shouldReplaceLines = Objects.nonNull(replacedLines);
        for (String oracleKey : oracleKeys) {
            if (StringUtils.contains(replacedLine, oracleKey)) {
                //If the list is empty
                List<Integer> lineNumbers = occurrences.get(oracleKey);
                if (CollectionUtils.isEmpty(lineNumbers)) {
                    lineNumbers = new ArrayList<>();
                }
                lineNumbers.add(lineNumber);
                occurrences.put(oracleKey, lineNumbers);
                if (shouldReplaceLines) {
                    replacedLine = StringUtils.replaceAll(replacedLine, oracleKey, databaseProperties.getProperty(oracleKey));
                    //line = line.replace(oracleKey, oracleConfig.getOracleProperties().getProperty(oracleKey));
                }
            }
        }
        if(shouldReplaceLines){
            replacedLines.add(replacedLine);
        }
        return occurrences;
    }
}
