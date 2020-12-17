package co.edu.unal.migration.commerceanalyzer.controllers.rest;

import co.edu.unal.migration.commerceanalyzer.utils.DatabaseConfigUtils;
import co.edu.unal.migration.commerceanalyzer.dto.WordCrawlerInputParams;
import co.edu.unal.migration.commerceanalyzer.dto.WordFound;
import co.edu.unal.migration.commerceanalyzer.services.WordCrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.util.*;

import static co.edu.unal.migration.commerceanalyzer.constants.CommerceAnalyzerConstants.ORACLE;

@RestController
@RequestMapping("/analyzer")
public class CommerceAnalyzerController {
    @Autowired
    private WordCrawlerService wordCrawlerService;

    @RequestMapping("/")
    public ResponseEntity<Set<WordFound>> searchWordInDirectory() {
        Set<WordFound> wordFoundSet = new HashSet<>();
        return ResponseEntity.ok(wordCrawlerService.searchWordInDirectory(wordFoundSet, "GETDATE()"));
    }

    @RequestMapping("/java-files")
    public ResponseEntity<Set<String>> listJavaFiles() {
        return ResponseEntity.ok(wordCrawlerService.listJavaFilesFromRootDirectory());
    }

    @RequestMapping("/oracle-properties")
    public ResponseEntity<Properties> listOracleProperties()  throws IOException {
        return ResponseEntity.ok(DatabaseConfigUtils.getDatabaseProperties(ORACLE));
    }

    @RequestMapping("/mysql-properties")
    public ResponseEntity<Properties> listMysqlOracleProperties()  throws IOException {
        return ResponseEntity.ok(DatabaseConfigUtils.getDatabaseProperties(ORACLE));
    }

    @RequestMapping("/find-oracle-keywords")
    public ResponseEntity<Set<WordFound>> findOracleKeywords(@Valid WordCrawlerInputParams wordCrawlerInputParams){
        return ResponseEntity.ok(wordCrawlerService.findOracleKeysInJavaFiles(wordCrawlerInputParams));
    }
}

